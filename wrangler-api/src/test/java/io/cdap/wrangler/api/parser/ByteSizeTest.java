package io.cdap.wrangler.api.parser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class ByteSizeTest {
    @Test
    public void testByteSizeParsing() {
        ByteSize size1 = new ByteSize("1024B");
        assertEquals(1024L, size1.getBytes());
        
        ByteSize size2 = new ByteSize("1KB");
        assertEquals(1024L, size2.getBytes());
        
        ByteSize size3 = new ByteSize("1.5MB");
        assertEquals(1.5 * 1024 * 1024, size3.getBytes(), 0.001);
        
        ByteSize size4 = new ByteSize("2GB");
        assertEquals(2L * 1024 * 1024 * 1024, size4.getBytes());
    }

    @Test
    public void testInvalidByteSize() {
        assertThrows(IllegalArgumentException.class, () -> new ByteSize("10XB"));
        assertThrows(IllegalArgumentException.class, () -> new ByteSize("KB"));
        assertThrows(IllegalArgumentException.class, () -> new ByteSize("1.2.3MB"));
    }

    @Test
    public void testUnitConversion() {
        ByteSize size = new ByteSize("1MB");
        assertEquals(1.0, size.getMB(), 0.001);
        assertEquals(1024.0, size.getKB(), 0.001);
        assertEquals(1.0/1024, size.getGB(), 0.0001);
    }
}