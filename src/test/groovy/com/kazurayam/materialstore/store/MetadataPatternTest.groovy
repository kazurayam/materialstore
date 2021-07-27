package com.kazurayam.materialstore.store

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*

class MetadataPatternTest {

    @Test
    void test_create_with_keySet() {
        Metadata metadata = new Metadata(["profile":"ProjectionEnv","category":"screenshot"])
        MetadataPattern pattern = MetadataPattern.create(["profile"] as Set, metadata)
        assertNotNull(pattern)
        assertTrue(pattern.containsKey("profile"))
        assertFalse(pattern.containsKey("category"))
    }

    @Test
    void test_create_without_keySet() {
        Metadata metadata = new Metadata(["profile":"ProjectionEnv","category":"screenshot"])
        MetadataPattern pattern = MetadataPattern.create(metadata)
        assertNotNull(pattern)
        assertTrue(pattern.containsKey("profile"))
        assertTrue(pattern.containsKey("category"))
    }
}
