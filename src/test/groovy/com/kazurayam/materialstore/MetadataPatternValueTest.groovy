package com.kazurayam.materialstore

import org.junit.jupiter.api.Test
import java.util.regex.Pattern

import static org.junit.jupiter.api.Assertions.*

class MetadataPatternValueTest {

    @Test
    void test_basics_StringBackedInstance() {
        MetadataPatternValue metadataPatternValue = MetadataPatternValue.of("DevEnv")
        assertTrue(metadataPatternValue.isString())
        assertFalse(metadataPatternValue.isPattern())
        assertEquals("DevEnv", metadataPatternValue.toString())
    }

    @Test
    void test_basics_PatternBackedInstance() {
        MetadataPatternValue metadataPatternValue = MetadataPatternValue.of(Pattern.compile(".*"))
        assertFalse(metadataPatternValue.isString())
        assertTrue(metadataPatternValue.isPattern())
        assertEquals("re:.*", metadataPatternValue.toString())
    }

    @Test
    void test_compareTo_StringBackedInstance() {
        MetadataPatternValue a = MetadataPatternValue.of("a")
        MetadataPatternValue b = MetadataPatternValue.of("b")
        MetadataPatternValue A = MetadataPatternValue.of("A")
        assertEquals(0, a.compareTo(a))
        assertEquals(-1, a.compareTo(b))
        assertEquals(1, a.compareTo(A))
    }

    @Test
    void test_compareTo_PatternBackedInstance() {
        MetadataPatternValue a = MetadataPatternValue.of(Pattern.compile("a"))
        MetadataPatternValue b = MetadataPatternValue.of(Pattern.compile("b"))
        MetadataPatternValue A = MetadataPatternValue.of(Pattern.compile("A"))
        assertEquals(0, a.compareTo(a))
        assertEquals(-1, a.compareTo(b))
        assertEquals(1, a.compareTo(A))
    }

    @Test
    void test_matches_StringBackedInstance() {
        MetadataPatternValue metadataPatternValue = MetadataPatternValue.of("DevEnv")
        assertTrue(metadataPatternValue.matches("DevEnv"))
        assertFalse(metadataPatternValue.matches("URL.host"))
    }

    @Test
    void test_matches_PatternBackedInstance() {
        MetadataPatternValue metadataPatternValue = MetadataPatternValue.of(Pattern.compile(".*Env"))
        assertTrue(metadataPatternValue.matches("ProductionEnv"))
        assertFalse(metadataPatternValue.matches("foo"))
    }
}
