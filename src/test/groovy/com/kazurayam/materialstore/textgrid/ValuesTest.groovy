package com.kazurayam.materialstore.textgrid


import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertTrue

class ValuesTest {

    @Test
    void test_equals_true() {
        List<String> l1 = ["a"];
        List<String> l2 = ["a"]
        Values rv1 = new Values(l1)
        Values rv2 = new Values(l2)
        assertTrue(rv1 == rv2)
    }

    @Test
    void test_equals_false_by_size() {
        List<String> l1 = ["a"];
        List<String> l2 = ["a", "a"]
        Values rv1 = new Values(l1)
        Values rv2 = new Values(l2)
        assertFalse(rv1 == rv2)
    }

    @Test
    void test_equals_false_by_value() {
        List<String> l1 = ["a"];
        List<String> l2 = ["b"]
        Values rv1 = new Values(l1)
        Values rv2 = new Values(l2)
        assertFalse(rv1 == rv2)
    }

    @Test
    void test_hashCode() {
        List<String> l1 = ["a"]
        Values rv1 = new Values(l1)
        assertTrue(rv1.hashCode() > 0)
    }

    @Test
    void test_toString() {
        List<String> l1 = ["a"]
        Values rv1 = new Values(l1)
        assertEquals("[\"a\"]", rv1.toString())
    }

    @Test
    void test_compareTo_0() {
        List<String> l1 = ["a"];
        List<String> l2 = ["a"]
        Values rv1 = new Values(l1)
        Values rv2 = new Values(l2)
        assertEquals(0, rv1.compareTo(rv2))
    }

    @Test
    void test_compareTo_minus_by_value() {
        List<String> l1 = ["a"];
        List<String> l2 = ["b"]
        Values rv1 = new Values(l1)
        Values rv2 = new Values(l2)
        assertEquals(-1, rv1.compareTo(rv2))
    }

    @Test
    void test_compareTo_plus_by_value() {
        List<String> l1 = ["b"];
        List<String> l2 = ["a"]
        Values rv1 = new Values(l1)
        Values rv2 = new Values(l2)
        assertEquals(1, rv1.compareTo(rv2))
    }

    @Test
    void test_compareTo_minus_by_size() {
        List<String> l1 = ["a"];
        List<String> l2 = ["a", "b"]
        Values rv1 = new Values(l1)
        Values rv2 = new Values(l2)
        assertEquals(-1, rv1.compareTo(rv2))
    }

    @Test
    void test_compareTo_plus_by_size() {
        List<String> l1 = ["a", "b"];
        List<String> l2 = ["a"]
        Values rv1 = new Values(l1)
        Values rv2 = new Values(l2)
        assertEquals(1, rv1.compareTo(rv2))
    }
}
