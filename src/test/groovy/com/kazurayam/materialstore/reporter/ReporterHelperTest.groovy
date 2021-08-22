package com.kazurayam.materialstore.reporter

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*

class ReporterHelperTest {

    @Test
    void test_getStyleFromClasspath() {
        String style = ReporterHelper.loadStyleFromClasspath()
        assertNotNull(style)
        println style
        assertTrue(style.length() > 0)
    }
}
