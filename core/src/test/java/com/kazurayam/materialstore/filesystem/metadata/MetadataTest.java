package com.kazurayam.materialstore.filesystem.metadata;

import com.kazurayam.materialstore.filesystem.Metadata;
import com.kazurayam.materialstore.util.KeyValuePair;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class MetadataTest {

    @Test
    public void testParseQuery() {
        List<KeyValuePair> list = Metadata.parseQuery("a=x&b=y&b=z&c");
        assertEquals(4, list.size());
        assertEquals("a", list.get(0).getKey());
        assertEquals("x", list.get(0).getValue());
        assertEquals("b", list.get(1).getKey());
        assertEquals("y", list.get(1).getValue());
        assertEquals("b", list.get(2).getKey());
        assertEquals("z", list.get(2).getValue());
        assertEquals("c", list.get(3).getKey());
        assertNull(list.get(3).getValue());
    }
}
