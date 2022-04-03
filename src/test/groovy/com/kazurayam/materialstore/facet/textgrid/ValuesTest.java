package com.kazurayam.materialstore.facet.textgrid;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ValuesTest {
    @Test
    public void test_equals_true() {
        List<String> l1 = new ArrayList<>(Collections.singletonList("a"));
        List<String> l2 = new ArrayList<>(Collections.singletonList("a"));
        Values rv1 = new Values(l1);
        Values rv2 = new Values(l2);
        Assertions.assertEquals(rv1, rv2);
    }

    @Test
    public void test_equals_false_by_size() {
        List<String> l1 = new ArrayList<>(Collections.singletonList("a"));
        List<String> l2 = new ArrayList<>(Arrays.asList("a", "a"));
        Values rv1 = new Values(l1);
        Values rv2 = new Values(l2);
        Assertions.assertNotEquals(rv1, rv2);
    }

    @Test
    public void test_equals_false_by_value() {
        List<String> l1 = new ArrayList<>(Collections.singletonList("a"));
        List<String> l2 = new ArrayList<>(Collections.singletonList("b"));
        Values rv1 = new Values(l1);
        Values rv2 = new Values(l2);
        Assertions.assertNotEquals(rv1, rv2);
    }

    @Test
    public void test_hashCode() {
        List<String> l1 = new ArrayList<>(Collections.singletonList("a"));
        Values rv1 = new Values(l1);
        Assertions.assertTrue(rv1.hashCode() > 0);
    }

    @Test
    public void test_toString() {
        List<String> l1 = new ArrayList<>(Collections.singletonList("a"));
        Values rv1 = new Values(l1);
        Assertions.assertEquals("[\"a\"]", rv1.toString());
    }

    @Test
    public void test_compareTo_0() {
        List<String> l1 = new ArrayList<>(Collections.singletonList("a"));
        List<String> l2 = new ArrayList<>(Collections.singletonList("a"));
        Values rv1 = new Values(l1);
        Values rv2 = new Values(l2);
        Assertions.assertEquals(0, rv1.compareTo(rv2));
    }

    @Test
    public void test_compareTo_minus_by_value() {
        List<String> l1 = new ArrayList<>(Collections.singletonList("a"));
        List<String> l2 = new ArrayList<>(Collections.singletonList("b"));
        Values rv1 = new Values(l1);
        Values rv2 = new Values(l2);
        Assertions.assertEquals(-1, rv1.compareTo(rv2));
    }

    @Test
    public void test_compareTo_plus_by_value() {
        List<String> l1 = new ArrayList<>(Collections.singletonList("b"));
        List<String> l2 = new ArrayList<>(Collections.singletonList("a"));
        Values rv1 = new Values(l1);
        Values rv2 = new Values(l2);
        Assertions.assertEquals(1, rv1.compareTo(rv2));
    }

    @Test
    public void test_compareTo_minus_by_size() {
        List<String> l1 = new ArrayList<>(Collections.singletonList("a"));
        List<String> l2 = new ArrayList<>(Arrays.asList("a", "b"));
        Values rv1 = new Values(l1);
        Values rv2 = new Values(l2);
        Assertions.assertEquals(-1, rv1.compareTo(rv2));
    }

    @Test
    public void test_compareTo_plus_by_size() {
        List<String> l1 = new ArrayList<>(Arrays.asList("a", "b"));
        List<String> l2 = new ArrayList<>(Collections.singletonList("a"));
        Values rv1 = new Values(l1);
        Values rv2 = new Values(l2);
        Assertions.assertEquals(1, rv1.compareTo(rv2));
    }

}
