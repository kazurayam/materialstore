package com.kazurayam.materialstore.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KeyValuePairTest {

    @Test
    public void testSmoke() {
        KeyValuePair p = new KeyValuePair("a", "x");
        assertEquals("a", p.getKey());
        assertEquals("x", p.getValue());
    }

    @Test
    public void testNullKey() {
        Exception exception = assertThrows(NullPointerException.class, () -> {
            new KeyValuePair(null, "v");
        });
        String expectedMessage = "must not be null";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testNullValue() {
        KeyValuePair p = new KeyValuePair("c", null);
        assertEquals("c", p.getKey());
        assertNull(p.getValue());
    }
}
