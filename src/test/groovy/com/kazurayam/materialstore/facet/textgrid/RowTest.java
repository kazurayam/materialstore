package com.kazurayam.materialstore.facet.textgrid;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RowTest {

    @Test
    public void test_constructor() {
        List<String> list = new ArrayList<>(Arrays.asList("a", "b", "c"));
        KeyRange keyRange = new KeyRange(0, 1);
        Row rec = new Row(list, keyRange);
        Assertions.assertNotNull(rec);
    }

    @Test
    public void test_values() {
        List<String> list = new ArrayList<>(Arrays.asList("a", "b", "c"));
        KeyRange keyRange = new KeyRange(0, 1);
        Row rec = new Row(list, keyRange);
        Assertions.assertEquals(new Values(new ArrayList<>(Arrays.asList("a", "b", "c"))), rec.values());
    }

    @Test
    public void test_key() {
        List<String> list = new ArrayList<>(Arrays.asList("a", "b", "c"));
        KeyRange keyRange = new KeyRange(0, 1);
        Row rec = new Row(list, keyRange);
        Assertions.assertEquals(new Key(new ArrayList<>(Arrays.asList("a", "b", "c")), keyRange), rec.key());
    }

    @Test
    public void test_keyRange() {
        List<String> list = new ArrayList<>(Arrays.asList("a", "b", "c"));
        KeyRange keyRange = new KeyRange(0, 1);
        Row rec = new Row(list, keyRange);
        Assertions.assertEquals(new KeyRange(0, 1), rec.keyRange());
    }

    @Test
    public void test_keyEquals() {
        List<String> list1 = new ArrayList<>(Arrays.asList("a", "b", "c"));
        List<String> list2 = new ArrayList<>(Arrays.asList("a", "b", "c"));
        Row row1 = new Row(list1, new KeyRange(0, 1));
        Row row2 = new Row(list2, new KeyRange(0, 1));
        Assertions.assertTrue(row1.keyEquals(row2));
    }

    @Test
    public void test_equals_true() {
        List<String> row1 = new ArrayList<>(Arrays.asList("a", "b", "c"));
        List<String> row2 = new ArrayList<>(Arrays.asList("a", "b", "c"));
        Row r1 = new Row(row1, new KeyRange(0, 1));
        Row r2 = new Row(row2, new KeyRange(0, 1));
        Assertions.assertEquals(r1, r2);
    }

    @Test
    public void test_equals_false_by_value() {
        List<String> row1 = new ArrayList<>(Arrays.asList("a", "b", "c"));
        List<String> row2 = new ArrayList<>(Arrays.asList("A", "b", "c"));
        Row r1 = new Row(row1, new KeyRange(0, 1));
        Row r2 = new Row(row2, new KeyRange(0, 1));
        Assertions.assertNotEquals(r1, r2);
    }

    @Disabled
    @Test
    public void test_equals_false_by_keyRange() {
        List<String> row1 = new ArrayList<>(Arrays.asList("a", "b", "c"));
        List<String> row2 = new ArrayList<>(Arrays.asList("a", "b", "c"));
        Row r1 = new Row(row1, new KeyRange(0, 1));
        Row r2 = new Row(row2, new KeyRange(0, 1));
        Assertions.assertNotEquals(r1, r2);
    }

    @Test
    public void test_hashCode() {
        List<String> row1 = new ArrayList<>(Arrays.asList("a", "b", "c"));
        Row r1 = new Row(row1, new KeyRange(0, 1));
        Assertions.assertTrue(r1.hashCode() > 0);
    }

    @Test
    public void test_toString() {
        List<String> row1 = new ArrayList<>(Arrays.asList("a", "b", "c"));
        Row r1 = new Row(row1, new KeyRange(0, 1));
        System.out.println(r1);
        Assertions.assertEquals("{\"key\":[\"a\",\"b\"],\"values\":[\"a\", \"b\", \"c\"],\"keyRange\":[0, 1]}", r1.toString());
    }

    @Test
    public void test_compareTo_0() {
        List<String> row1 = new ArrayList<>(Arrays.asList("a", "b", "c"));
        List<String> row2 = new ArrayList<>(Arrays.asList("a", "b", "c"));
        Row r1 = new Row(row1, new KeyRange(0, 1));
        Row r2 = new Row(row2, new KeyRange(0, 1));
        Assertions.assertEquals(0, r1.compareTo(r2));
    }

}
