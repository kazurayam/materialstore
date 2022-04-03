package com.kazurayam.materialstore.facet.textgrid;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KeyTest {
    @Test
    public void test_constructor() {
        List<String> row = new ArrayList<>(Arrays.asList("a", "b", "c"));
        KeyRange keyRange = new KeyRange(0, 1);
        Key rowKey = new Key(row, keyRange);
        Assertions.assertNotNull(rowKey);
        Assertions.assertEquals(keyRange, rowKey.keyRange());
    }

    @Test
    public void test_keyElements() {
        List<String> row = new ArrayList<>(Arrays.asList("a", "b", "c"));
        KeyRange keyRange = new KeyRange(0, 1);
        Key rowKey = new Key(row, keyRange);
        Assertions.assertNotNull(rowKey);
        Assertions.assertEquals(new ArrayList<>(Arrays.asList("a", "b")), rowKey.keyElements());
    }

    @Test
    public void test_equals_true() {
        List<String> row1 = new ArrayList<>(Arrays.asList("a", "b", "c"));
        List<String> row2 = new ArrayList<>(Arrays.asList("a", "b", "c"));
        Key rk1 = new Key(row1, new KeyRange(0, 1));
        Key rk2 = new Key(row2, new KeyRange(0, 1));
        Assertions.assertEquals(rk1, rk2);
    }

    @Test
    public void test_equals_false_by_value() {
        List<String> row1 = new ArrayList<>(Arrays.asList("a", "b", "c"));
        List<String> row2 = new ArrayList<>(Arrays.asList("A", "b", "c"));
        Key rk1 = new Key(row1, new KeyRange(0, 1));
        Key rk2 = new Key(row2, new KeyRange(0, 1));
        Assertions.assertNotEquals(rk1, rk2);
    }

    @Disabled
    @Test
    public void test_equals_false_by_keyRange() {
        List<String> row1 = new ArrayList<>(Arrays.asList("a", "b", "c"));
        List<String> row2 = new ArrayList<>(Arrays.asList("a", "b", "c"));
        Key rk1 = new Key(row1, new KeyRange(0, 1));
        Key rk2 = new Key(row2, new KeyRange(0, 1));
        Assertions.assertNotEquals(rk1, rk2);
    }

    @Test
    public void test_hashCode() {
        List<String> row1 = new ArrayList<>(Arrays.asList("a", "b", "c"));
        Key rk1 = new Key(row1, new KeyRange(0, 1));
        Assertions.assertTrue(rk1.hashCode() > 0);
    }

    @Test
    public void test_toString() {
        List<String> row1 = new ArrayList<>(Arrays.asList("a", "b", "c"));
        Key rk1 = new Key(row1, new KeyRange(0, 1));
        Assertions.assertEquals("[\"a\",\"b\"]", rk1.toString());
    }

    @Test
    public void test_compareTo_0() {
        List<String> row1 = new ArrayList<>(Arrays.asList("a", "b", "c"));
        List<String> row2 = new ArrayList<>(Arrays.asList("a", "b", "c"));
        Key rk1 = new Key(row1, new KeyRange(0, 1));
        Key rk2 = new Key(row2, new KeyRange(0, 1));
        Assertions.assertEquals(0, rk1.compareTo(rk2));
    }

    @Test
    public void test_compareTo_minus() {
        List<String> row1 = new ArrayList<>(Arrays.asList("a", "b", "c"));
        List<String> row2 = new ArrayList<>(Arrays.asList("b", "c", "d"));
        Key rk1 = new Key(row1, new KeyRange(0, 1));
        Key rk2 = new Key(row2, new KeyRange(0, 1));
        Assertions.assertEquals(-1, rk1.compareTo(rk2));
    }

    @Test
    public void test_compareTo_plus() {
        List<String> row1 = new ArrayList<>(Arrays.asList("b", "c", "d"));
        List<String> row2 = new ArrayList<>(Arrays.asList("a", "b", "c"));
        Key rk1 = new Key(row1, new KeyRange(0, 1));
        Key rk2 = new Key(row2, new KeyRange(0, 1));
        Assertions.assertEquals(1, rk1.compareTo(rk2));
    }

    @Disabled
    @Test
    public void test_compareTo_shorter() {
        List<String> row1 = new ArrayList<>(Arrays.asList("a", "b", "c"));
        List<String> row2 = new ArrayList<>(Arrays.asList("a", "b", "c"));
        Key rk1 = new Key(row1, new KeyRange(0, 1));
        Key rk2 = new Key(row2, new KeyRange(0, 1));
        Assertions.assertEquals(-1, rk1.compareTo(rk2));
    }

    @Disabled
    @Test
    public void test_compareTo_longer() {
        List<String> row1 = new ArrayList<>(Arrays.asList("a", "b", "c"));
        List<String> row2 = new ArrayList<>(Arrays.asList("a", "b", "c"));
        Key rk1 = new Key(row1, new KeyRange(0, 1));
        Key rk2 = new Key(row2, new KeyRange(0, 1));
        Assertions.assertEquals(1, rk1.compareTo(rk2));
    }

}
