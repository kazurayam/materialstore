package com.kazurayam.materialstore.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringUtilsTest {

    @Test
    public void test_indentLines() {
        String text = "abc\ndef";
        String expected = "    abc\n    def\n";
        String actual = StringUtils.indentLines(text);
        assertEquals(expected, actual);
    }
}
