package com.kazurayam.materialstore.metadata

import com.kazurayam.materialstore.metadata.IgnoringMetadataKeys
import groovy.xml.MarkupBuilder
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*

class IgnoringMetadataKeysTest {

    @Test
    void test_of() {
        IgnoringMetadataKeys ignoringMetadataKeys = IgnoringMetadataKeys.of("profile", "URL.hostname")
        assertNotNull(ignoringMetadataKeys)
        assertTrue(ignoringMetadataKeys.contains("profile"))
        assertEquals(2, ignoringMetadataKeys.size())
    }

    @Test
    void test_toString() {
        IgnoringMetadataKeys ignoringMetadataKeys = IgnoringMetadataKeys.of("profile", "URL.hostname")
        String str = ignoringMetadataKeys.toString()
        assertNotNull(str)
        assertEquals("""{"URL.hostname", "profile"}""", str)
    }

    @Test
    void test_toSpanSequence() {
        IgnoringMetadataKeys ignoringMetadataKeys = IgnoringMetadataKeys.of("profile", "URL.hostname")
        StringWriter sw = new StringWriter()
        MarkupBuilder mb = new MarkupBuilder(sw)
        ignoringMetadataKeys.toSpanSequence(mb)
        String str = sw.toString()
        assertNotNull(str)
        //println str
        assertTrue(str.contains("ignoring-key"))
    }
}
