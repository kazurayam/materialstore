package com.kazurayam.materials.store

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertTrue

class MetadataTest {

    @Test
    void test_constructor_with_List() {
        Metadata metadata = new Metadata(["foo"])
        assertEquals("foo", metadata.entry(0))
    }

    @Test
    void test_add_get() {
        Metadata metadata = new Metadata()
        metadata.add("foo")
        assertEquals("foo", metadata.entry(0))
    }

    @Test
    void test_addAll() {
        Metadata metadata = new Metadata()
        metadata.addAll(["foo"])
        assertEquals("foo", metadata.entry(0))
    }

    @Test
    void test_size() {
        Metadata metadata = new Metadata()
        metadata.add("foo")
        assertEquals(1, metadata.size())
    }

    @Test
    void test_match_simplest() {
        Metadata base = new Metadata("a")
        MetadataPattern pattern = new MetadataPattern("a")
        assertTrue(base.match(pattern))
    }

    @Test
    void test_match_empty_pattern_matches_everything() {
        Metadata base = new Metadata("a")
        MetadataPattern pattern = new MetadataPattern()
        assertTrue(base.match(pattern))
    }

    @Test
    void test_match_asterisk_pattern_matches_everything() {
        Metadata base = new Metadata("a")
        MetadataPattern pattern = new MetadataPattern("*")
        assertTrue(base.match(pattern))
    }

    @Test
    void test_match_longer_pattern_doesnt_match() {
        Metadata base = new Metadata("a")
        MetadataPattern pattern = new MetadataPattern("*", "b")
        assertFalse(base.match(pattern), "the pattern longer than the base should fail")
    }

    @Test
    void test_match_demonstrative() {
        Metadata base = new Metadata("ProductionEnv", "http://demoaut.katalon.com")
        MetadataPattern pattern = new MetadataPattern("ProductionEnv")
        assertTrue(base.match(pattern))
    }



    @Test
    void test_compareTo_empty_instances_should_be_equal() {
        Metadata x = new Metadata()
        Metadata y = new Metadata()
        assertTrue(x.compareTo(y) == 0)
    }

    @Test
    void test_compareTo_simply_equals() {
        Metadata x = new Metadata("a", "b", "c")
        Metadata y = new Metadata("a", "b", "c")
        assertTrue(x.compareTo(y) == 0)
    }

    @Test
    void test_compareTo_x_is_longer_than_y() {
        Metadata x = new Metadata("a")
        Metadata y = new Metadata()
        assertTrue(x.compareTo(y) == 1, "x.size()=${x.size()}, y.size()=${y.size()}")
    }

    @Test
    void test_compareTo_x_is_shorter_than_y() {
        Metadata x = new Metadata()
        Metadata y = new Metadata("a")
        assertTrue(x.compareTo(y) == -1, "x.size()=${x.size()}, y.size()=${y.size()}")
    }

    @Test
    void test_compareTo_x_is_smaller_than_y() {
        Metadata x = new Metadata("a")
        Metadata y = new Metadata("b")
        assertTrue(x.compareTo(y) == -1, "x.size()=${x.size()}, y.size()=${y.size()}")
    }

    @Test
    void test_compareTo_x_is_larger_than_y() {
        Metadata x = new Metadata("b")
        Metadata y = new Metadata("a")
        assertTrue(x.compareTo(y) == 1, "x.size()=${x.size()}, y.size()=${y.size()}")
    }

    @Test
    void test_equals_positive() {
        Metadata x = new Metadata("a", "b", "c")
        Metadata y = new Metadata("a", "b", "c")
        assertTrue (x == y)
    }

    @Test
    void test_equals_negative() {
        Metadata x = new Metadata("a", "b", "c")
        Metadata y = new Metadata("0", "1", "2")
        assertTrue (x != y)
    }

}
