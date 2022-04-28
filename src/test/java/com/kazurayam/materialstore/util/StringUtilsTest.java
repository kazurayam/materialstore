package com.kazurayam.materialstore.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringUtilsTest {

    @Test
    public void test_indentLines() {
        String text = "abc\ndef";
        String expected = "    abc\n    def\n";
        String actual = StringUtils.indentLines(text);
        assertEquals(expected, actual);
    }

    @Test
    public void test_toList() {
        String text = "a b c\nd e f";
        List<String> expected = Arrays.asList("a b c", "d e f");
        List<String> actual = StringUtils.toList(text);
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < actual.size(); i++) {
            assertEquals(actual.get(i), expected.get(i));
        }
    }
}
