package io.cdap.wrangler.api.parser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class TimeDurationTest {
    @Test
    public void testTimeDurationParsing() {
        TimeDuration time1 = new TimeDuration("1000ns");
        assertEquals(1000L, time1.getNanoseconds());
        
        TimeDuration time2 = new TimeDuration("500ms");
        assertEquals(500 * 1000 * 1000L, time2.getNanoseconds());
        
        TimeDuration time3 = new TimeDuration("1.5s");
        assertEquals((long)(1.5 * 1000 * 1000 * 1000), time3.getNanoseconds());
        
        TimeDuration time4 = new TimeDuration("2h");
        assertEquals(2L * 60 * 60 * 1000 * 1000 * 1000, time4.getNanoseconds());
    }

    @Test
    public void testInvalidTimeDuration() {
        assertThrows(IllegalArgumentException.class, () -> new TimeDuration("10ys"));
        assertThrows(IllegalArgumentException.class, () -> new TimeDuration("ms"));
        assertThrows(IllegalArgumentException.class, () -> new TimeDuration("1.2.3s"));
    }

    @Test
    public void testUnitConversion() {
        TimeDuration time = new TimeDuration("1s");
        assertEquals(1000.0, time.getMilliseconds(), 0.001);
        assertEquals(1.0, time.getSeconds(), 0.001);
        assertEquals(1.0/60, time.getMinutes(), 0.0001);
    }
}