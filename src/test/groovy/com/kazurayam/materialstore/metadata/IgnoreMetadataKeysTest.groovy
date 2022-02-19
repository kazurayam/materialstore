package com.kazurayam.materialstore.metadata


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
        assertEquals("""{"URL.hostname", "profile"}""", str)
    }

    @Test
    void test_toSpanSequence() {
        IgnoreMetadataKeys ignoreMetadataKeys =
                new IgnoreMetadataKeys.Builder()
                        .ignoreKeys("profile", "URL.hostname")
                        .build()
        StringWriter sw = new StringWriter()
        MarkupBuilder mb = new MarkupBuilder(sw)
        ignoreMetadataKeys.toSpanSequence(mb)
        String str = sw.toString()
        assertNotNull(str)
        //println str
        assertTrue(str.contains("ignored-key"))
    }
}
