package com.kazurayam.materialstore.filesystem.metadata


import org.junit.jupiter.api.Test
import java.util.regex.Pattern

import static org.junit.jupiter.api.Assertions.*
import com.kazurayam.materialstore.filesystem.QueryOnMetadata.QValue

class QValueTest {

    @Test
    void test_basics_StringBackedInstance() {
        QValue qValue = QValue.of("DevEnv")
        assertTrue(qValue.isString())
        assertFalse(qValue.isPattern())
        assertEquals("DevEnv", qValue.toString())
    }

    @Test
    void test_basics_PatternBackedInstance() {
        QValue qValue = QValue.of(Pattern.compile(".*"))
        assertFalse(qValue.isString())
        assertTrue(qValue.isPattern())
        assertEquals("re:.*", qValue.toString())
    }

    @Test
    void test_compareTo_StringBackedInstance() {
        QValue a = QValue.of("a")
        QValue b = QValue.of("b")
        QValue A = QValue.of("A")
        assertEquals(0, a.compareTo(a))
        assertEquals(-1, a.compareTo(b))
        assertEquals(1, a.compareTo(A))
    }

    @Test
    void test_compareTo_PatternBackedInstance() {
        QValue a = QValue.of(Pattern.compile("a"))
        QValue b = QValue.of(Pattern.compile("b"))
        QValue A = QValue.of(Pattern.compile("A"))
        assertEquals(0, a.compareTo(a))
        assertEquals(-1, a.compareTo(b))
        assertEquals(1, a.compareTo(A))
    }

    @Test
    void test_matches_StringBackedInstance() {
        QValue qValue = QValue.of("DevEnv")
        assertTrue(qValue.matches("DevEnv"))
        assertFalse(qValue.matches("URL.host"))
    }

    @Test
    void test_matches_PatternBackedInstance() {
        QValue qValue = QValue.of(Pattern.compile(".*Env"))
        assertTrue(qValue.matches("ProductionEnv"))
        assertFalse(qValue.matches("foo"))
    }

    /*
     * #80 want to identify 2 URLs that have different value of URL.query;
     * but if you apply a RegEx, you can regard then as a pair
     */
    @Test
    void test_matches_PatternBackedInstance_URL_query() {
        QValue qValue = QValue.of(Pattern.compile("\\w{32}"))
        assertTrue(qValue.matches("856008caa5eb66df68595e734e59580d"))
    }
}
