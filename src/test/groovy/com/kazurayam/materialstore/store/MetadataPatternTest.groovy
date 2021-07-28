package com.kazurayam.materialstore.store

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*

class MetadataPatternTest {

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
}
