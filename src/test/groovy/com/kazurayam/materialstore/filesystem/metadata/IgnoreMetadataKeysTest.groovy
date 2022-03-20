package com.kazurayam.materialstore.filesystem.metadata


import groovy.xml.MarkupBuilder
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*

class IgnoreMetadataKeysTest {

    @Test
    void test_of() {
        IgnoreMetadataKeys ignoreMetadataKeys =
                new IgnoreMetadataKeys.Builder()
                        .ignoreKeys("profile", "URL.hostname")
                        .build()
        assertNotNull(ignoreMetadataKeys)
        assertTrue(ignoreMetadataKeys.contains("profile"))
        assertEquals(2, ignoreMetadataKeys.size())
    }

    @Test
    void test_toString() {
        IgnoreMetadataKeys ignoreMetadataKeys =
                new IgnoreMetadataKeys.Builder()
                        .ignoreKeys("profile", "URL.hostname")
                        .build()
        String str = ignoreMetadataKeys.toString()
        assertNotNull(str)
        assertEquals("""["URL.hostname", "profile"]""", str)
    }

}
