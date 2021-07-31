package com.kazurayam.materialstore

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*

class JsonUtilTest {

    @Test
    void test_escapeAsJsonString_Backspace() {
        assertEquals("\\b", JsonUtil.escapeAsJsonString("\b"))
    }

    @Test
    void test_escapeAsJsonString_Formfeed() {
        assertEquals("\\f", JsonUtil.escapeAsJsonString("\f"))
    }

    @Test
    void test_escapeAsJsonString_Newline() {
        assertEquals("\\n", JsonUtil.escapeAsJsonString("\n"))
    }

    @Test
    void test_escapeAsJsonString_Carriagereturn() {
        assertEquals("\\r", JsonUtil.escapeAsJsonString("\r"))
    }

    @Test
    void test_escapeAsJsonString_Tab() {
        assertEquals("\\t", JsonUtil.escapeAsJsonString("\t"))
    }

    @Test
    void test_escapeAsJsonString_Doublequote() {
        assertEquals("\\\"", JsonUtil.escapeAsJsonString('\"'))
    }

    @Test
    void test_escapeAsJsonString_Backslash() {
        assertEquals("\\\\", JsonUtil.escapeAsJsonString("\\"))
    }

    @Test
    void test_escapeAsJsonString_default() {
        assertEquals("愛", JsonUtil.escapeAsJsonString("愛"))
    }

}
