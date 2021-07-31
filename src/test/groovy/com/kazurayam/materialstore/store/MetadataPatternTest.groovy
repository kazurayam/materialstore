package com.kazurayam.materialstore.store

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*

class MetadataPatternTest {

    @Test
    void test_compareTo() {
        assertEquals(0, MetadataPattern.ANY <=> MetadataPattern.ANY)
    }

    @Test
    void test_create_with_MetadataIgnoredKeys() {
        Metadata metadata = new Metadata(["profile":"ProjectionEnv","category":"screenshot"])
        MetadataIgnoredKeys ignoredKeys = new MetadataIgnoredKeys.Builder().ignoreKey("profile").build()
        MetadataPattern pattern = MetadataPattern.create(ignoredKeys, metadata)
        assertNotNull(pattern)
        assertFalse(pattern.containsKey("profile"))
        assertTrue(pattern.containsKey("category"))
    }

    @Test
    void test_create_without_ignoredKeys() {
        Metadata metadata = new Metadata(["profile":"ProjectionEnv","category":"screenshot"])
        MetadataPattern pattern = MetadataPattern.create(metadata)
        assertNotNull(pattern)
        assertTrue(pattern.containsKey("profile"))
        assertTrue(pattern.containsKey("category"))
    }

    @Test
    void test_constructing_ANY() {
        MetadataPattern mp = new MetadataPattern(["*":"*"])
        assertNotNull(mp)
        assertEquals(1, mp.size())
        assertEquals("*", mp.get("*"))
    }

    @Test
    void test_constructing_NULLOBJECT() {
        MetadataPattern mp = new MetadataPattern([:])
        assertNotNull(mp)
        assertEquals(0, mp.size())
    }

    @Test
    void test_ANY_toString() {
        String expected = "{\"*\":\"*\"}"
        String actual = MetadataPattern.ANY.toString()
        assertEquals(expected, actual)
    }

    @Test
    void test_NULLOBJECT_toString() {
        String expected = "{}"
        String actual = MetadataPattern.NULL_OBJECT.toString()
        assertEquals(expected, actual)
    }
}
