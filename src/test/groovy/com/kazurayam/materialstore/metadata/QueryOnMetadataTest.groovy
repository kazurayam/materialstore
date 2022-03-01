package com.kazurayam.materialstore.metadata


import groovy.xml.MarkupBuilder
import groovy.json.JsonOutput
import org.junit.jupiter.api.Test
import java.util.regex.Pattern

import static org.junit.jupiter.api.Assertions.*

class QueryOnMetadataTest {

    @Test
    void test_ANY() {
        QueryOnMetadata query = QueryOnMetadata.ANY
        assertNotNull(query)
        assertEquals(1, query.size())
        assertTrue(query.containsKey("*"))
        assertEquals("re:.*", query.getAsString("*"))
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
        Metadata metadata = Metadata.builder([
                "profile":"ProjectionEnv",
                "category":"screenshot"])
                .build()
        IgnoreMetadataKeys ignoreMetadataKeys =
                new IgnoreMetadataKeys.Builder()
                        .ignoreKey("profile")
                        .build()
        QueryOnMetadata query = QueryOnMetadata.builder(metadata, ignoreMetadataKeys).build()
        assertNotNull(query)
        assertFalse(query.containsKey("profile"))
        assertTrue(query.containsKey("category"))
    }

    @Test
    void test_create_without_IgnoreMetadataKeys() {
        Metadata metadata = Metadata.builder([
                "profile":"ProjectionEnv",
                "category":"screenshot"])
                .build()
        QueryOnMetadata query = QueryOnMetadata.builder(metadata).build()
        assertNotNull(query)
        assertTrue(query.containsKey("profile"))
        assertTrue(query.containsKey("category"))
    }

    @Test
    void test_getDescription() {
        QueryOnMetadata mp = QueryOnMetadata.builder([
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
        Metadata metadata = Metadata.builder(["profile": "ProductionEnv"]).build()
        assertTrue(query.matches(metadata))
    }

    @Test
    void test_matches_falsy() {
        QueryOnMetadata query = QueryOnMetadata.builder(["profile": "ProductionEnv"]).build()
        Metadata metadata = Metadata.builder(["foo": "bar"]).build()
        assertFalse(query.matches(metadata))
    }

    @Test
    void test_matches_multiple_AND_conditions() {
        QueryOnMetadata query = QueryOnMetadata.builder()
                .put("profile", "ProductionEnv")
                .put("URL.file", Pattern.compile(".*")).build()
        //
        Metadata metadata1 = Metadata.builder(["profile": "ProductionEnv", "URL.file": "/"]).build()
        assertTrue(query.matches(metadata1))
        //
        Metadata metadata2 = Metadata.builder(["profile": "DevelopEnv", "URL.file": "/"]).build()
        assertFalse(query.matches(metadata2))
        //
        Metadata metadata3 = Metadata.builder(["profile": "ProductionEnv"]).build()
        assertFalse(query.matches(metadata3))
        //
        Metadata metadata4 = Metadata.builder(["URL.file": "/"]).build()
        assertFalse(query.matches(metadata4))
    }

    @Test
    void test_matches_2_paths_with_semantic_version() {
        Metadata metadataLeft = Metadata.builder(
                ["profile": "prod","URL.path": "/npm/bootstrap@5.1.0/dist/js/bootstrap.bundle.min.js"])
                .build()
        Metadata metadataRight = Metadata.builder(
                ["profile": "dev", "URL.path": "/npm/bootstrap@5.1.3-alpha/dist/js/bootstrap.bundle.min.js"])
                .build()
        IgnoreMetadataKeys ignoreMetadataKeys =
                new IgnoreMetadataKeys.Builder()
                        .ignoreKey("profile")
                        .build()
        QueryOnMetadata query = QueryOnMetadata.builder(metadataRight, ignoreMetadataKeys).build()
        assert query.matches(metadataLeft)
    }

    @Test
    void test_matches_truthy() {
        QueryOnMetadata query = QueryOnMetadata.builder(["profile": "ProductionEnv"]).build()
        Metadata metadata = Metadata.builder(["profile": "ProductionEnv"]).build()
        assertTrue(query.matches(metadata))
    }

    @Test
    void test_NULLOBJECT() {
        QueryOnMetadata query = QueryOnMetadata.NULL_OBJECT
        assertNotNull(query)
        assertEquals(0, query.size())
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
        QueryOnMetadata query = QueryOnMetadata.builder(metadata).build()
        StringWriter sw = new StringWriter()
        MarkupBuilder mb = new MarkupBuilder(sw)
        mb.div() {
            query.toSpanSequence(mb)
        }
        String markup = sw.toString()
        assertNotNull(markup)
        //println markup
        assertTrue(markup.contains("matched-value"))
    }

    @Test
    void test_toSpanSequence_regex() {
        QueryOnMetadata query = QueryOnMetadata.builder()
                .put("*", Pattern.compile(".*"))
                .build()
        Metadata metadata = Metadata.builder()
                .put("profile", "DevEnv")
                .put("URL.host", "demoaut-mimic.kazurayam.com").build()
        StringWriter sw = new StringWriter()
        MarkupBuilder mb = new MarkupBuilder(sw)
        mb.div() {
            query.toSpanSequence(mb)
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
        QueryOnMetadata query = QueryOnMetadata.builder(metadata, ignoreMetadataKeys).build()
        String expected = '''{"B":"b", "C":"c", "a":"a"}'''
        String actual = query.toString()
        println JsonOutput.prettyPrint(actual)
        assertEquals(expected, actual)
    }

}
