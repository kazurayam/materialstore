package com.kazurayam.materialstore

import groovy.xml.MarkupBuilder
import org.junit.jupiter.api.Test
import java.util.regex.Pattern
import static org.junit.jupiter.api.Assertions.*

class MetadataPatternTest {

    @Test
    void test_compareTo() {
        assertEquals(0, MetadataPattern.ANY <=> MetadataPattern.ANY)
    }

    @Test
    void test_create_with_IgnoringMetadataKeys() {
        Metadata metadata = Metadata.builderWithMap([
                "profile":"ProjectionEnv",
                "category":"screenshot"])
                .build()
        IgnoringMetadataKeys ignoringMetadataKeys = IgnoringMetadataKeys.of("profile")
        MetadataPattern pattern = MetadataPattern.builderWithMetadata(metadata, ignoringMetadataKeys).build()
        assertNotNull(pattern)
        assertFalse(pattern.containsKey("profile"))
        assertTrue(pattern.containsKey("category"))
    }

    @Test
    void test_create_without_ignoringMetadataKeys() {
        Metadata metadata = Metadata.builderWithMap([
                "profile":"ProjectionEnv",
                "category":"screenshot"])
                .build()
        MetadataPattern pattern = MetadataPattern.builderWithMetadata(metadata).build()
        assertNotNull(pattern)
        assertTrue(pattern.containsKey("profile"))
        assertTrue(pattern.containsKey("category"))
    }

    @Test
    void test_ANY() {
        MetadataPattern mp = MetadataPattern.ANY
        assertNotNull(mp)
        assertEquals(1, mp.size())
        assertEquals(Pattern.compile(".*").toString(), mp.get("*").toString())
    }

    @Test
    void test_NULLOBJECT() {
        MetadataPattern mp = MetadataPattern.NULL_OBJECT
        assertNotNull(mp)
        assertEquals(0, mp.size())
    }

    @Test
    void test_ANY_toString() {
        String expected = "{\"*\":\"re:.*\"}"
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
        Metadata metadata = Metadata.builder()
                .put("a","a")
                .put("C","c")
                .put("B","b")
                .build()
        IgnoringMetadataKeys ignoringMetadataKeys = IgnoringMetadataKeys.NULL_OBJECT
        MetadataPattern pattern = MetadataPattern.builderWithMetadata(metadata, ignoringMetadataKeys).build()
        String expected = '''{"B":"b", "C":"c", "a":"a"}'''
        String actual = pattern.toString()
        assertEquals(expected, actual)
    }

    @Test
    void test_toSpanSequence() {
        Metadata metadata = Metadata.builder()
                .put("profile", "DevEnv")
                .put("URL.host", "demoaut-mimic.kazurayam.com").build()
        MetadataPattern pattern = MetadataPattern.builderWithMetadata(metadata).build()
        StringWriter sw = new StringWriter()
        MarkupBuilder mb = new MarkupBuilder(sw)
        mb.div() {
            pattern.toSpanSequence(mb)
        }
        String markup = sw.toString()
        assertNotNull(markup)
        //println markup
        assertTrue(markup.contains("matched-value"))
    }
}
