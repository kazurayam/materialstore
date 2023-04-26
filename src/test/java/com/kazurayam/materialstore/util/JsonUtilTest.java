package com.kazurayam.materialstore.util;

import com.kazurayam.materialstore.util.JsonUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JsonUtilTest {
    @Test
    public void test_escapeAsJsonString_Backspace() {
        Assertions.assertEquals("\\b", JsonUtil.escapeAsJsonString("\b"));
    }

    @Test
    public void test_escapeAsJsonString_Formfeed() {
        Assertions.assertEquals("\\f", JsonUtil.escapeAsJsonString("\f"));
    }

    @Test
    public void test_escapeAsJsonString_Newline() {
        Assertions.assertEquals("\\n", JsonUtil.escapeAsJsonString("\n"));
    }

    @Test
    public void test_escapeAsJsonString_Carriagereturn() {
        Assertions.assertEquals("\\r", JsonUtil.escapeAsJsonString("\r"));
    }

    @Test
    public void test_escapeAsJsonString_Tab() {
        Assertions.assertEquals("\\t", JsonUtil.escapeAsJsonString("\t"));
    }

    @Test
    public void test_escapeAsJsonString_Doublequote() {
        Assertions.assertEquals("\\\"", JsonUtil.escapeAsJsonString("\""));
    }

    @Test
    public void test_escapeAsJsonString_Backslash() {
        Assertions.assertEquals("\\\\", JsonUtil.escapeAsJsonString("\\"));
    }

    @Test
    public void test_escapeAsJsonString_default() {
        Assertions.assertEquals("愛", JsonUtil.escapeAsJsonString("愛"));
    }

}
