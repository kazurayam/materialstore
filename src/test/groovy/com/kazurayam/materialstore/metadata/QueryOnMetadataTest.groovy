package com.kazurayam.materialstore.metadata


import groovy.xml.MarkupBuilder
import groovy.json.JsonOutput
import org.junit.jupiter.api.Test
import java.util.regex.Pattern

import static org.junit.jupiter.api.Assertions.*

class QueryOnMetadataTest {

    @Test
    void test_ANY() {
        QueryOnMetadata mp = QueryOnMetadata.ANY
        assertNotNull(mp)
        assertEquals(1, mp.size())
        assertTrue(mp.containsKey("*"))
        assertEquals("re:.*", mp.getAsString("*"))
    }

    @Test
    void test_ANY_toString() {
        String expected = "{\"*\":\"re:.*\"}"
        String actual = QueryOnMetadata.ANY.toString()
        assertEquals(expected, actual)
    }

    @Test
    void test_compareTo() {
        assertEquals(0, QueryOnMetadata.ANY <=> QueryOnMetadata.ANY)
    }

    @Test
    void test_create_with_IgnoreMetadataKeys() {
        Metadata metadata = Metadata.builderWithMap([
                "profile":"ProjectionEnv",
                "category":"screenshot"])
                .build()
        IgnoreMetadataKeys ignoreMetadataKeys =
                new IgnoreMetadataKeys.Builder()
                        .ignoreKey("profile")
                        .build()
        QueryOnMetadata pattern = QueryOnMetadata.builderWithMetadata(metadata, ignoreMetadataKeys).build()
        assertNotNull(pattern)
        assertFalse(pattern.containsKey("profile"))
        assertTrue(pattern.containsKey("category"))
    }

    @Test
    void test_create_without_IgnoreMetadataKeys() {
        Metadata metadata = Metadata.builderWithMap([
                "profile":"ProjectionEnv",
                "category":"screenshot"])
                .build()
        QueryOnMetadata pattern = QueryOnMetadata.builderWithMetadata(metadata).build()
        assertNotNull(pattern)
        assertTrue(pattern.containsKey("profile"))
        assertTrue(pattern.containsKey("category"))
    }

    @Test
    void test_getDescription() {
        QueryOnMetadata mp = QueryOnMetadata.builderWithMap([
                "URL.path": "/",
                "profile": "Flaskr_ProductionEnv",
                "step":"6"
        ]).build()
        SortKeys orderArtifacts = new SortKeys("step", "profile")
        String description = mp.getDescription(orderArtifacts)
        assertEquals('''{"step":"6", "profile":"Flaskr_ProductionEnv", "URL.path":"/"}''',
                description)
    }

    @Test
    void test_matches_ANY() {
        QueryOnMetadata query = QueryOnMetadata.ANY
        Metadata metadata = Metadata.builderWithMap(["profile": "ProductionEnv"]).build()
        assertTrue(query.matches(metadata))
    }

    @Test
    void test_matches_falsy() {
        QueryOnMetadata query = QueryOnMetadata.builderWithMap(["profile": "ProductionEnv"]).build()
        Metadata metadata = Metadata.builderWithMap(["foo": "bar"]).build()
        assertFalse(query.matches(metadata))
    }

    @Test
    void test_matches_multiple_AND_conditions() {
        QueryOnMetadata query = QueryOnMetadata.builder()
                .put("profile", "ProductionEnv")
                .put("URL.file", Pattern.compile(".*")).build()
        //
        Metadata metadata1 = Metadata.builderWithMap(["profile": "ProductionEnv", "URL.file": "/"]).build()
        assertTrue(query.matches(metadata1))
        //
        Metadata metadata2 = Metadata.builderWithMap(["profile": "DevelopEnv", "URL.file": "/"]).build()
        assertFalse(query.matches(metadata2))
        //
        Metadata metadata3 = Metadata.builderWithMap(["profile": "ProductionEnv"]).build()
        assertFalse(query.matches(metadata3))
        //
        Metadata metadata4 = Metadata.builderWithMap(["URL.file": "/"]).build()
        assertFalse(query.matches(metadata4))
    }

    @Test
    void test_matches_2_paths_with_semantic_version() {
        Metadata metadataLeft = Metadata.builderWithMap(
                ["profile": "prod","URL.path": "/npm/bootstrap@5.1.0/dist/js/bootstrap.bundle.min.js"])
                .build()
        Metadata metadataRight = Metadata.builderWithMap(
                ["profile": "dev", "URL.path": "/npm/bootstrap@5.1.3-alpha/dist/js/bootstrap.bundle.min.js"])
                .build()
        IgnoreMetadataKeys ignoreMetadataKeys =
                new IgnoreMetadataKeys.Builder()
                        .ignoreKey("profile")
                        .build()
        QueryOnMetadata query = QueryOnMetadata.builderWithMetadata(metadataRight, ignoreMetadataKeys).build()
        assert query.matches(metadataLeft)
    }

    @Test
    void test_matches_truthy() {
        QueryOnMetadata query = QueryOnMetadata.builderWithMap(["profile": "ProductionEnv"]).build()
        Metadata metadata = Metadata.builderWithMap(["profile": "ProductionEnv"]).build()
        assertTrue(query.matches(metadata))
    }

    @Test
    void test_NULLOBJECT() {
        QueryOnMetadata mp = QueryOnMetadata.NULL_OBJECT
        assertNotNull(mp)
        assertEquals(0, mp.size())
    }


    @Test
    void test_NULLOBJECT_toString() {
        String expected = "{}"
        String actual = QueryOnMetadata.NULL_OBJECT.toString()
        assertEquals(expected, actual)
    }


    @Test
    void test_toSpanSequence() {
        Metadata metadata = Metadata.builder()
                .put("profile", "DevEnv")
                .put("URL.host", "demoaut-mimic.kazurayam.com").build()
        QueryOnMetadata pattern = QueryOnMetadata.builderWithMetadata(metadata).build()
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

    @Test
    void test_toSpanSequence_regex() {
        QueryOnMetadata pattern = QueryOnMetadata.builder()
                .put("*", Pattern.compile(".*"))
                .build()
        Metadata metadata = Metadata.builder()
                .put("profile", "DevEnv")
                .put("URL.host", "demoaut-mimic.kazurayam.com").build()
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

    @Test
    void test_toString_keys_should_be_sorted() {
        Metadata metadata = Metadata.builder()
                .put("a","a")
                .put("C","c")
                .put("B","b")
                .build()
        IgnoreMetadataKeys ignoreMetadataKeys = IgnoreMetadataKeys.NULL_OBJECT
        QueryOnMetadata pattern = QueryOnMetadata.builderWithMetadata(metadata, ignoreMetadataKeys).build()
        String expected = '''{"B":"b", "C":"c", "a":"a"}'''
        String actual = pattern.toString()
        println JsonOutput.prettyPrint(actual)
        assertEquals(expected, actual)
    }

}
