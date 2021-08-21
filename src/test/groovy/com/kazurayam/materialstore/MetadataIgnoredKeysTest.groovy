package com.kazurayam.materialstore

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*

class MetadataIgnoredKeysTest {

    @Test
    void test_of() {
        MetadataIgnoredKeys ignoredKeys = MetadataIgnoredKeys.of("profile", "URL.hostname")
        assertNotNull(ignoredKeys)
        assertTrue(ignoredKeys.contains("profile"))
        assertEquals(2, ignoredKeys.size())
    }

    /*
    @Test
    void test_building_noArg() {
        MetadataIgnoredKeys ignoredKeys = new MetadataIgnoredKeys.Builder().build()
        assertNotNull(ignoredKeys)
    }

    @Test
    void test_building_witArg() {
        MetadataPattern pattern = MetadataPattern.builderWithMap(["profile":"ProductionEnv"]).build()
        MetadataIgnoredKeys ignoredKeys = new MetadataIgnoredKeys.Builder(pattern).build()
        assertNotNull(ignoredKeys)
        assertEquals(1, ignoredKeys.size())
        assertTrue(ignoredKeys.contains("profile"))
        ignoredKeys.each {key ->
            assertTrue(key.length() > 0)
        }
    }
     */
}
