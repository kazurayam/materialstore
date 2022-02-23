package com.kazurayam.materialstore.metadata


import org.junit.jupiter.api.Test
import java.util.regex.Pattern

import static org.junit.jupiter.api.Assertions.*

class QueryOnMetadataValueTest {

    @Test
    void test_basics_StringBackedInstance() {
        QueryOnMetadataValue queryOnMetadataValue = QueryOnMetadataValue.of("DevEnv")
        assertTrue(queryOnMetadataValue.isString())
        assertFalse(queryOnMetadataValue.isPattern())
        assertEquals("DevEnv", queryOnMetadataValue.toString())
    }

    @Test
    void test_basics_PatternBackedInstance() {
        QueryOnMetadataValue queryOnMetadataValue = QueryOnMetadataValue.of(Pattern.compile(".*"))
        assertFalse(queryOnMetadataValue.isString())
        assertTrue(queryOnMetadataValue.isPattern())
        assertEquals("re:.*", queryOnMetadataValue.toString())
    }

    @Test
    void test_compareTo_StringBackedInstance() {
        QueryOnMetadataValue a = QueryOnMetadataValue.of("a")
        QueryOnMetadataValue b = QueryOnMetadataValue.of("b")
        QueryOnMetadataValue A = QueryOnMetadataValue.of("A")
        assertEquals(0, a.compareTo(a))
        assertEquals(-1, a.compareTo(b))
        assertEquals(1, a.compareTo(A))
    }

    @Test
    void test_compareTo_PatternBackedInstance() {
        QueryOnMetadataValue a = QueryOnMetadataValue.of(Pattern.compile("a"))
        QueryOnMetadataValue b = QueryOnMetadataValue.of(Pattern.compile("b"))
        QueryOnMetadataValue A = QueryOnMetadataValue.of(Pattern.compile("A"))
        assertEquals(0, a.compareTo(a))
        assertEquals(-1, a.compareTo(b))
        assertEquals(1, a.compareTo(A))
    }

    @Test
    void test_matches_StringBackedInstance() {
        QueryOnMetadataValue queryOnMetadataValue = QueryOnMetadataValue.of("DevEnv")
        assertTrue(queryOnMetadataValue.matches("DevEnv"))
        assertFalse(queryOnMetadataValue.matches("URL.host"))
    }

    @Test
    void test_matches_PatternBackedInstance() {
        QueryOnMetadataValue queryOnMetadataValue = QueryOnMetadataValue.of(Pattern.compile(".*Env"))
        assertTrue(queryOnMetadataValue.matches("ProductionEnv"))
        assertFalse(queryOnMetadataValue.matches("foo"))
    }

    /*
     * #80 want to identify 2 URLs that have different value of URL.query;
     * but if you apply a RegEx, you can regard then as a pair
     */
    @Test
    void test_matches_PatternBackedInstance_URL_query() {
        QueryOnMetadataValue queryOnMetadataValue = QueryOnMetadataValue.of(Pattern.compile("\\w{32}"))
        assertTrue(queryOnMetadataValue.matches("856008caa5eb66df68595e734e59580d"))
    }
}
