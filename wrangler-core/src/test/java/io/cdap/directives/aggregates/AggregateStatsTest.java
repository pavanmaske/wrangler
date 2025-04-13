package io.cdap.wrangler.directives.aggregate;

import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.executor.TestingRig;
import io.cdap.wrangler.parser.TextDirectives;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class AggregateStatsTest {
    @Test
    public void testBasicAggregation() throws Exception {
        String[] directives = new String[] {
            "aggregate-stats :size :time total_size total_time"
        };

        List<Row> rows = Arrays.asList(
            new Row("size", "1KB").add("time", "100ms"),
            new Row("size", "2KB").add("time", "200ms"),
            new Row("size", "1.5KB").add("time", "150ms")
        );

        List<Row> results = TestingRig.execute(directives, rows);
        assertEquals(1, results.size());
        
        Row result = results.get(0);
        // 1KB + 2KB + 1.5KB = 4.5KB = 4608 bytes
        assertEquals(4608.0, (Double) result.getValue("total_size"), 0.001);
        // 100ms + 200ms + 150ms = 450ms = 0.45 seconds
        assertEquals(0.45, (Double) result.getValue("total_time"), 0.001);
    }

    @Test
    public void testWithOutputUnits() throws Exception {
        String[] directives = new String[] {
            "aggregate-stats :size :time total_size_mb total_time_s 1MB 1s"
        };

        List<Row> rows = Arrays.asList(
            new Row("size", "1MB").add("time", "1000ms"),
            new Row("size", "2MB").add("time", "2000ms"),
            new Row("size", "1.5MB").add("time", "1500ms")
        );

        List<Row> results = TestingRig.execute(directives, rows);
        assertEquals(1, results.size());
        
        Row result = results.get(0);
        // 1 + 2 + 1.5 = 4.5MB
        assertEquals(4.5, (Double) result.getValue("total_size_mb"), 0.001);
        // 1 + 2 + 1.5 = 4.5 seconds
        assertEquals(4.5, (Double) result.getValue("total_time_s"), 0.001);
    }

    @Test
    public void testAverageAggregation() throws Exception {
        String[] directives = new String[] {
            "aggregate-stats :size :time avg_size avg_time 1KB 1ms avg"
        };

        List<Row> rows = Arrays.asList(
            new Row("size", "1KB").add("time", "100ms"),
            new Row("size", "2KB").add("time", "200ms"),
            new Row("size", "3KB").add("time", "300ms")
        );

        List<Row> results = TestingRig.execute(directives, rows);
        assertEquals(1, results.size());
        
        Row result = results.get(0);
        // (1 + 2 + 3)/3 = 2KB
        assertEquals(2.0, (Double) result.getValue("avg_size"), 0.001);
        // (100 + 200 + 300)/3 = 200ms
        assertEquals(200.0, (Double) result.getValue("avg_time"), 0.001);
    }

    @Test
    public void testMixedUnits() throws Exception {
        String[] directives = new String[] {
            "aggregate-stats :size :time total_size_mb total_time_s 1MB 1s"
        };

        List<Row> rows = Arrays.asList(
            new Row("size", "1024KB").add("time", "1000ms"),
            new Row("size", "1MB").add("time", "1s"),
            new Row("size", "0.5GB").add("time", "30m")
        );

        List<Row> results = TestingRig.execute(directives, rows);
        assertEquals(1, results.size());
        
        Row result = results.get(0);
        // 1024KB = 1MB + 1MB + 0.5GB = 512MB → 514MB total
        assertEquals(514.0, (Double) result.getValue("total_size_mb"), 0.001);
        // 1s + 1s + 1800s → 1802s total
        assertEquals(1802.0, (Double) result.getValue("total_time_s"), 0.001);
    }

    @Test
    public void testNullValues() throws Exception {
        String[] directives = new String[] {
            "aggregate-stats :size :time total_size total_time"
        };

        List<Row> rows = Arrays.asList(
            new Row("size", "1KB").add("time", null),
            new Row("size", null).add("time", "200ms"),
            new Row("size", "1.5KB").add("time", "150ms")
        );

        List<Row> results = TestingRig.execute(directives, rows);
        assertEquals(1, results.size());
        
        Row result = results.get(0);
        // 1KB + 1.5KB = 2.5KB = 2560 bytes
        assertEquals(2560.0, (Double) result.getValue("total_size"), 0.001);
        // 200ms + 150ms = 350ms = 0.35 seconds
        assertEquals(0.35, (Double) result.getValue("total_time"), 0.001);
    }
}