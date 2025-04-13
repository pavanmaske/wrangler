package io.cdap.wrangler.directives.aggregate;

import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.executor.TestingRig;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class AggregateStatsEdgeCasesTest {
    @Test
    public void testEmptyInput() throws Exception {
        String[] directives = new String[] {
            "aggregate-stats :size :time total_size total_time"
        };

        List<Row> rows = Collections.emptyList();
        List<Row> results = TestingRig.execute(directives, rows);
        assertEquals(1, results.size());
        
        Row result = results.get(0);
        assertEquals(0.0, (Double) result.getValue("total_size"), 0.001);
        assertEquals(0.0, (Double) result.getValue("total_time"), 0.001);
    }

    @Test
    public void testSingleRow() throws Exception {
        String[] directives = new String[] {
            "aggregate-stats :size :time total_size total_time"
        };

        List<Row> rows = Collections.singletonList(
            new Row("size", "1KB").add("time", "100ms")
        );

        List<Row> results = TestingRig.execute(directives, rows);
        assertEquals(1, results.size());
        
        Row result = results.get(0);
        assertEquals(1024.0, (Double) result.getValue("total_size"), 0.001);
        assertEquals(0.1, (Double) result.getValue("total_time"), 0.001);
    }

    @Test
    public void testLargeValues() throws Exception {
        String[] directives = new String[] {
            "aggregate-stats :size :time total_size total_time 1TB 1d"
        };

        List<Row> rows = Arrays.asList(
            new Row("size", "500GB").add("time", "12h"),
            new Row("size", "0.5TB").add("time", "12h")
        );

        List<Row> results = TestingRig.execute(directives, rows);
        assertEquals(1, results.size());
        
        Row result = results.get(0);
        // 500GB + 0.5TB = 1TB → should show as 1 in TB units
        assertEquals(1.0, (Double) result.getValue("total_size"), 0.001);
        // 12h + 12h = 24h = 1d
        assertEquals(1.0, (Double) result.getValue("total_time"), 0.001);
    }
}