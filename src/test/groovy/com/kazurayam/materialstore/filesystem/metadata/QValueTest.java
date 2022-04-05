package com.kazurayam.materialstore.filesystem.metadata;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

public class QValueTest {
    @Test
    public void test_basics_StringBackedInstance() {
        QValue qValue = QValue.of("DevEnv");
        Assertions.assertTrue(qValue.isString());
        Assertions.assertFalse(qValue.isPattern());
        Assertions.assertEquals("DevEnv", qValue.toString());
    }

    @Test
    public void test_basics_PatternBackedInstance() {
        QValue qValue = QValue.of(Pattern.compile(".*"));
        Assertions.assertFalse(qValue.isString());
        Assertions.assertTrue(qValue.isPattern());
        Assertions.assertEquals("re:.*", qValue.toString());
    }

    @Test
    public void test_compareTo_StringBackedInstance() {
        QValue a = QValue.of("a");
        QValue b = QValue.of("b");
        QValue A = QValue.of("A");
        Assertions.assertEquals(0, a.compareTo(a));
        Assertions.assertTrue(a.compareTo(b) < 0);
        Assertions.assertTrue(0 < a.compareTo(A));
    }

    @Test
    public void test_compareTo_PatternBackedInstance() {
        QValue a = QValue.of(Pattern.compile("a"));
        QValue b = QValue.of(Pattern.compile("b"));
        QValue A = QValue.of(Pattern.compile("A"));
        Assertions.assertEquals(0, a.compareTo(a));
        Assertions.assertTrue(a.compareTo(b) < 0);
        Assertions.assertTrue(0 < a.compareTo(A));
    }

    @Test
    public void test_matches_StringBackedInstance() {
        QValue qValue = QValue.of("DevEnv");
        Assertions.assertTrue(qValue.matches("DevEnv"));
        Assertions.assertFalse(qValue.matches("URL.host"));
    }

    @Test
    public void test_matches_PatternBackedInstance() {
        QValue qValue = QValue.of(Pattern.compile(".*Env"));
        Assertions.assertTrue(qValue.matches("ProductionEnv"));
        Assertions.assertFalse(qValue.matches("foo"));
    }

    @Test
    public void test_matches_PatternBackedInstance_URL_query() {
        QValue qValue = QValue.of(Pattern.compile("\\w{32}"));
        Assertions.assertTrue(qValue.matches("856008caa5eb66df68595e734e59580d"));
    }

}
