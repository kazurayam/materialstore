package com.kazurayam.materialstore.textgrid


import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertNotEquals
import static org.junit.jupiter.api.Assertions.assertTrue

class RowTest {

    @Test
    void test_constructor() {
        List<String> list = ["a", "b", "c"]
        Range<Integer> keyRange = 0..1
        Row rec = new Row(list, keyRange)
        assertNotNull(rec)
    }

    @Test
    void test_values() {
        List<String> list = ["a", "b", "c"]
        Range keyRange = 0..1
        Row rec = new Row(list, keyRange)
        assertEquals(new Values(["a", "b", "c"]), rec.values())
    }

    @Test
    void test_key() {
        List<String> list = ["a", "b", "c"]
        Range keyRange = 0..1
        Row rec = new Row(list, keyRange)
        assertEquals(new Key(["a", "b", "c"], 0..1), rec.key())
    }


    @Test
    void test_keyRange() {
        List<String> list = ["a", "b", "c"]
        Range keyRange = 0..1
        Row rec = new Row(list, keyRange)
        assertEquals(0..1, rec.keyRange())
    }


    @Test
    void test_keyEquals() {
        List<String> list1 = ["a", "b", "c"]
        List<String> list2 = ["a", "b", "c"]
        Row row1 = new Row(list1, 0..1)
        Row row2 = new Row(list2, 0..1)
        assertTrue(row1.keyEquals(row2))
    }

    @Test
    void test_equals_true() {
        List<String> row1 = ["a", "b", "c"]
        List<String> row2 = ["a", "b", "c"]
        Row r1 = new Row(row1, 0..1)
        Row r2 = new Row(row2, 0..1)
        assertEquals(r1, r2)
    }

    @Test
    void test_equals_false_by_value() {
        List<String> row1 = ["a", "b", "c"]
        List<String> row2 = ["A", "b", "c"]
        Row r1 = new Row(row1, 0..1)
        Row r2 = new Row(row2, 0..1)
        assertNotEquals(r1, r2)
    }

    @Test
    void test_equals_false_by_keyRange() {
        List<String> row1 = ["a", "b", "c"]
        List<String> row2 = ["a", "b", "c"]
        Row r1 = new Row(row1, 0..1)
        Row r2 = new Row(row2, 0..2)
        assertNotEquals(r1, r2)
    }

    @Test
    void test_hashCode() {
        List<String> row1 = ["a", "b", "c"]
        Row r1 = new Row(row1, 0..1)
        assertTrue(r1.hashCode() > 0)
    }


    @Test
    void test_toString() {
        List<String> row1 = ["a", "b", "c"]
        Row r1 = new Row(row1, 0..1)
        println r1.toString()
        assertEquals('''{"key":["a","b"],"values":["a", "b", "c"],"keyRange":[0, 1]}''', r1.toString())
    }


    @Test
    void test_compareTo_0() {
        List<String> row1 = ["a", "b", "c"]
        List<String> row2 = ["a", "b", "c"]
        Row r1 = new Row(row1, 0..1)
        Row r2 = new Row(row2, 0..1)
        assertEquals(0, r1 <=> r2)
    }

}
