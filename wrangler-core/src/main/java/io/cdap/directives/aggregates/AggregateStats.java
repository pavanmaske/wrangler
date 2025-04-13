package io.cdap.wrangler.directives.aggregate;

import io.cdap.wrangler.api.*;
import io.cdap.wrangler.api.parser.*;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.TimeDuration;
import io.cdap.wrangler.executor.Context;

import java.util.List;

@PublicEvolving
public class AggregateStats implements Directive, AggregateDirective {
    private static final String BYTE_STORE_SUFFIX = "_bytes";
    private static final String TIME_STORE_SUFFIX = "_nanos";
    private static final String COUNT_STORE_SUFFIX = "_count";

    private String sizeColumn;
    private String timeColumn;
    private String outputSizeColumn;
    private String outputTimeColumn;
    private String sizeOutputUnit = "bytes";
    private String timeOutputUnit = "seconds";
    private AggregationType aggregationType = AggregationType.SUM;

    @Override
    public UsageDefinition define() {
        return UsageDefinition.builder()
            .required("sizeColumn", TokenType.IDENTIFIER)
            .required("timeColumn", TokenType.IDENTIFIER)
            .required("outputSizeColumn", TokenType.IDENTIFIER)
            .required("outputTimeColumn", TokenType.IDENTIFIER)
            .optional("sizeUnit", TokenType.BYTE_SIZE)
            .optional("timeUnit", TokenType.TIME_DURATION)
            .optional("aggregation", TokenType.IDENTIFIER)
            .build();
    }

    @Override
    public void initialize(Arguments args) throws DirectiveParseException {
        this.sizeColumn = ((Identifier) args.value("sizeColumn")).value();
        this.timeColumn = ((Identifier) args.value("timeColumn")).value();
        this.outputSizeColumn = ((Identifier) args.value("outputSizeColumn")).value();
        this.outputTimeColumn = ((Identifier) args.value("outputTimeColumn")).value();

        if (args.contains("sizeUnit")) {
            ByteSize sizeUnit = (ByteSize) args.value("sizeUnit");
            this.sizeOutputUnit = sizeUnit.toString().replaceAll("[^a-zA-Z]", "").toLowerCase();
        }

        if (args.contains("timeUnit")) {
            TimeDuration timeUnit = (TimeDuration) args.value("timeUnit");
            this.timeOutputUnit = timeUnit.toString().replaceAll("[^a-zA-Z]", "").toLowerCase();
        }

        if (args.contains("aggregation")) {
            String aggType = ((Identifier) args.value("aggregation")).value().toLowerCase();
            switch (aggType) {
                case "sum":
                    this.aggregationType = AggregationType.SUM;
                    break;
                case "avg":
                    this.aggregationType = AggregationType.AVG;
                    break;
                default:
                    throw new DirectiveParseException(
                        "Invalid aggregation type '" + aggType + "'. Supported types: sum, avg");
            }
        }
    }

    @Override
    public void destroy() {
        // no-op
    }

    @Override
    public List<Row> execute(List<Row> rows, ExecutorContext context) throws DirectiveExecutionException {
        Store<Long> sizeStore = context.getStore(sizeColumn + BYTE_STORE_SUFFIX, Long.class);
        Store<Long> timeStore = context.getStore(timeColumn + TIME_STORE_SUFFIX, Long.class);
        Store<Integer> countStore = context.getStore(sizeColumn + COUNT_STORE_SUFFIX, Integer.class);

        for (Row row : rows) {
            try {
                // Process byte size
                Object sizeObj = row.getValue(sizeColumn);
                if (sizeObj != null) {
                    ByteSize size = new ByteSize(sizeObj.toString());
                    sizeStore.increment(size.getBytes());
                }

                // Process time duration
                Object timeObj = row.getValue(timeColumn);
                if (timeObj != null) {
                    TimeDuration time = new TimeDuration(timeObj.toString());
                    timeStore.increment(time.getNanoseconds());
                }

                countStore.increment(1);
            } catch (Exception e) {
                throw new DirectiveExecutionException(
                    String.format("Error processing row %s: %s", row, e.getMessage()), e);
            }
        }

        return rows;
    }

    @Override
    public List<Row> finalize(ExecutorContext context) throws DirectiveExecutionException {
        Store<Long> sizeStore = context.getStore(sizeColumn + BYTE_STORE_SUFFIX, Long.class);
        Store<Long> timeStore = context.getStore(timeColumn + TIME_STORE_SUFFIX, Long.class);
        Store<Integer> countStore = context.getStore(sizeColumn + COUNT_STORE_SUFFIX, Integer.class);

        long totalBytes = sizeStore.get();
        long totalNanos = timeStore.get();
        int count = countStore.get();

        // Convert to requested units
        double outputSize = convertBytes(totalBytes, sizeOutputUnit);
        double outputTime = convertNanos(totalNanos, timeOutputUnit);

        // Apply aggregation type
        if (aggregationType == AggregationType.AVG && count > 0) {
            outputSize = outputSize / count;
            outputTime = outputTime / count;
        }

        Row result = new Row();
        result.add(outputSizeColumn, outputSize);
        result.add(outputTimeColumn, outputTime);

        // Clear stores for potential reuse
        sizeStore.reset();
        timeStore.reset();
        countStore.reset();

        return Collections.singletonList(result);
    }

    private double convertBytes(long bytes, String unit) {
        switch (unit.toLowerCase()) {
            case "kb": return bytes / 1024.0;
            case "mb": return bytes / (1024.0 * 1024.0);
            case "gb": return bytes / (1024.0 * 1024.0 * 1024.0);
            case "tb": return bytes / (1024.0 * 1024.0 * 1024.0 * 1024.0);
            case "pb": return bytes / (1024.0 * 1024.0 * 1024.0 * 1024.0 * 1024.0);
            default: return bytes; // bytes
        }
    }

    private double convertNanos(long nanos, String unit) {
        switch (unit.toLowerCase()) {
            case "us": return nanos / 1000.0;
            case "ms": return nanos / (1000.0 * 1000.0);
            case "s": return nanos / (1000.0 * 1000.0 * 1000.0);
            case "m": return nanos / (1000.0 * 1000.0 * 1000.0 * 60.0);
            case "h": return nanos / (1000.0 * 1000.0 * 1000.0 * 60.0 * 60.0);
            case "d": return nanos / (1000.0 * 1000.0 * 1000.0 * 60.0 * 60.0 * 24.0);
            default: return nanos; // nanoseconds
        }
    }

    private enum AggregationType {
        SUM, AVG
    }
}