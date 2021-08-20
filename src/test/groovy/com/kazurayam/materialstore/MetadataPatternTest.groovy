package com.kazurayam.materialstore

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*

class MetadataPatternTest {

    @Test
    void test_compareTo() {
        assertEquals(0, MetadataPattern.ANY <=> MetadataPattern.ANY)
    }

    @Test
    void test_create_with_MetadataIgnoredKeys() {
        Metadata metadata = new MetadataImpl.Builder(
                ["profile":"ProjectionEnv", "category":"screenshot"]).build()
        MetadataIgnoredKeys ignoredKeys = new MetadataIgnoredKeys.Builder().ignoreKey("profile").build()
        MetadataPattern pattern = new MetadataPattern.Builder(metadata, ignoredKeys).build()
        assertNotNull(pattern)
        assertFalse(pattern.containsKey("profile"))
        assertTrue(pattern.containsKey("category"))
    }

    @Test
    void test_create_without_ignoredKeys() {
        Metadata metadata = new MetadataImpl.Builder(
                ["profile":"ProjectionEnv", "category":"screenshot"]).build()
        MetadataPattern pattern = new MetadataPattern.Builder(metadata).build()
        assertNotNull(pattern)
        assertTrue(pattern.containsKey("profile"))
        assertTrue(pattern.containsKey("category"))
    }

    @Test
    void test_constructing_ANY() {
        MetadataPattern mp = new MetadataPattern.Builder(["*":"*"]).build()
        assertNotNull(mp)
        assertEquals(1, mp.size())
        assertEquals("*", mp.get("*"))
    }

    @Test
    void test_constructing_NULLOBJECT() {
        MetadataPattern mp = new MetadataPattern.Builder([:]).build()
        assertNotNull(mp)
        assertEquals(0, mp.size())
    }

    @Test
    void test_ANY_toString() {
        String expected = "{\"*\":\"regex:.*\"}"
        String actual = MetadataPattern.ANY.toString()
        assertEquals(expected, actual)
    }

    @Test
    void test_NULLOBJECT_toString() {
        String expected = "{}"
        String actual = MetadataPattern.NULL_OBJECT.toString()
        assertEquals(expected, actual)
    }

    @Test
    void test_toString_keys_should_be_sorted() {
        Metadata metadata = new MetadataImpl.Builder()
                .put("a","a")
                .put("C","c")
                .put("B","b")
                .build()
        MetadataIgnoredKeys ignoredKeys = MetadataIgnoredKeys.NULL_OBJECT
        MetadataPattern pattern = new MetadataPattern.Builder(metadata, ignoredKeys).build()
        String expected = '''{"B":"b", "C":"c", "a":"a"}'''
        String actual = pattern.toString()
        assertEquals(expected, actual)
    }
}
