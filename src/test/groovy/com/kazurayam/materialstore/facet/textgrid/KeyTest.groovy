package com.kazurayam.materialstore.facet.textgrid

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotEquals
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertTrue

class KeyTest {

    @Test
    void test_constructor() {
        List<String> row = ["a", "b", "c"]
        KeyRange keyRange = new KeyRange(0, 1);
        Key rowKey = new Key(row, keyRange)
        assertNotNull(rowKey)
        assertEquals(keyRange, rowKey.keyRange())
    }

    @Test
    void test_keyElements() {
        List<String> row = ["a", "b", "c"]
        KeyRange keyRange = new KeyRange(0,1)
        Key rowKey = new Key(row, keyRange)
        assertNotNull(rowKey)
        assertEquals(["a", "b"], rowKey.keyElements())
    }

    @Test
    void test_equals_true() {
        List<String> row1 = ["a", "b", "c"]
        List<String> row2 = ["a", "b", "c"]
        Key rk1 = new Key(row1, new KeyRange(0,1))
        Key rk2 = new Key(row2, new KeyRange(0,1))
        assertEquals(rk1, rk2)
    }

    @Test
    void test_equals_false_by_value() {
        List<String> row1 = ["a", "b", "c"]
        List<String> row2 = ["A", "b", "c"]
        Key rk1 = new Key(row1, new KeyRange(0,1))
        Key rk2 = new Key(row2, new KeyRange(0,1))
        assertNotEquals(rk1, rk2)
    }

    @Disabled
    @Test
    void test_equals_false_by_keyRange() {
        List<String> row1 = ["a", "b", "c"]
        List<String> row2 = ["a", "b", "c"]
        Key rk1 = new Key(row1, new KeyRange(0,1))
        Key rk2 = new Key(row2, new KeyRange(0,1))
        assertNotEquals(rk1, rk2)
    }

    @Test
    void test_hashCode() {
        List<String> row1 = ["a", "b", "c"]
        Key rk1 = new Key(row1, new KeyRange(0,1))
        assertTrue(rk1.hashCode() > 0)
    }

    @Test
    void test_toString() {
        List<String> row1 = ["a", "b", "c"]
        Key rk1 = new Key(row1, new KeyRange(0,1))
        assertEquals("[\"a\",\"b\"]", rk1.toString());
    }

    @Test
    void test_compareTo_0() {
        List<String> row1 = ["a", "b", "c"]
        List<String> row2 = ["a", "b", "c"]
        Key rk1 = new Key(row1, new KeyRange(0,1))
        Key rk2 = new Key(row2, new KeyRange(0,1))
        assertEquals(0, rk1 <=> rk2)
    }

    @Test
    void test_compareTo_minus() {
        List<String> row1 = ["a", "b", "c"]
        List<String> row2 = ["b", "c", "d"]
        Key rk1 = new Key(row1, new KeyRange(0, 1))
        Key rk2 = new Key(row2, new KeyRange(0, 1))
        assertEquals(-1, rk1 <=> rk2)
    }

    @Test
    void test_compareTo_plus() {
        List<String> row1 = ["b", "c", "d"]
        List<String> row2 = ["a", "b", "c"]
        Key rk1 = new Key(row1, new KeyRange(0,1))
        Key rk2 = new Key(row2, new KeyRange(0,1))
        assertEquals(1, rk1 <=> rk2)
    }

    @Disabled
    @Test
    void test_compareTo_shorter() {
        List<String> row1 = ["a", "b", "c"]
        List<String> row2 = ["a", "b", "c"]
        Key rk1 = new Key(row1, new KeyRange(0,1))
        Key rk2 = new Key(row2, new KeyRange(0,1))
        assertEquals(-1, rk1 <=> rk2)
    }

    @Disabled
    @Test
    void test_compareTo_longer() {
        List<String> row1 = ["a", "b", "c"]
        List<String> row2 = ["a", "b", "c"]
        Key rk1 = new Key(row1, new KeyRange(0,1))
        Key rk2 = new Key(row2, new KeyRange(0,1))
        assertEquals(1, rk1 <=> rk2)
    }
}
