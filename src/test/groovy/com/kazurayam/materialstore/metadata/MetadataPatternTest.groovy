package com.kazurayam.materialstore.metadata

import com.kazurayam.materialstore.diffartifact.DiffArtifactComparisonPriorities
import groovy.xml.MarkupBuilder
import groovy.json.JsonOutput
import org.junit.jupiter.api.Test
import java.util.regex.Pattern

import static org.junit.jupiter.api.Assertions.*

class MetadataPatternTest {

    @Test
    void test_ANY() {
        MetadataPattern mp = MetadataPattern.ANY
        assertNotNull(mp)
        assertEquals(1, mp.size())
        assertTrue(mp.containsKey("*"))
        assertEquals("re:.*", mp.getAsString("*"))
    }

    @Test
    void test_ANY_toString() {
        String expected = "{\"*\":\"re:.*\"}"
        String actual = MetadataPattern.ANY.toString()
        assertEquals(expected, actual)
    }

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
    void test_getDescription() {
        MetadataPattern mp = MetadataPattern.builderWithMap([
                "URL.path": "/",
                "profile": "Flaskr_ProductionEnv",
                "step":"6"
        ]).build()
        DiffArtifactComparisonPriorities orderArtifacts = new DiffArtifactComparisonPriorities("step", "profile")
        String description = mp.getDescription(orderArtifacts)
        assertEquals('''{"step":"6", "profile":"Flaskr_ProductionEnv", "URL.path":"/"}''',
                description)
    }

    @Test
    void test_matches_ANY() {
        MetadataPattern metadataPattern = MetadataPattern.ANY
        Metadata metadata = Metadata.builderWithMap(["profile": "ProductionEnv"]).build()
        assertTrue(metadataPattern.matches(metadata))
    }

    @Test
    void test_matches_falsy() {
        MetadataPattern metadataPattern = MetadataPattern.builderWithMap(["profile": "ProductionEnv"]).build()
        Metadata metadata = Metadata.builderWithMap(["foo": "bar"]).build()
        assertFalse(metadataPattern.matches(metadata))
    }

    @Test
    void test_matches_multiple_AND_conditions() {
        MetadataPattern metadataPattern = MetadataPattern.builder()
                .put("profile", "ProductionEnv")
                .put("URL.file", Pattern.compile(".*")).build()
        //
        Metadata metadata1 = Metadata.builderWithMap(["profile": "ProductionEnv", "URL.file": "/"]).build()
        assertTrue(metadataPattern.matches(metadata1))
        //
        Metadata metadata2 = Metadata.builderWithMap(["profile": "DevelopEnv", "URL.file": "/"]).build()
        assertFalse(metadataPattern.matches(metadata2))
        //
        Metadata metadata3 = Metadata.builderWithMap(["profile": "ProductionEnv"]).build()
        assertFalse(metadataPattern.matches(metadata3))
        //
        Metadata metadata4 = Metadata.builderWithMap(["URL.file": "/"]).build()
        assertFalse(metadataPattern.matches(metadata4))
    }

    @Test
    void test_matches_2_paths_with_semantic_version() {
        Metadata metadataLeft = Metadata.builderWithMap(
                ["profile": "prod","URL.path": "/npm/bootstrap@5.1.0/dist/js/bootstrap.bundle.min.js"])
                .build()
        Metadata metadataRight = Metadata.builderWithMap(
                ["profile": "dev", "URL.path": "/npm/bootstrap@5.1.3-alpha/dist/js/bootstrap.bundle.min.js"])
                .build()
        IgnoringMetadataKeys ignoringMetadataKeys = IgnoringMetadataKeys.of("profile")
        MetadataPattern metadataPattern = MetadataPattern.builderWithMetadata(metadataRight, ignoringMetadataKeys).build()
        assert metadataPattern.matches(metadataLeft)
    }

    @Test
    void test_matches_truthy() {
        MetadataPattern metadataPattern = MetadataPattern.builderWithMap(["profile": "ProductionEnv"]).build()
        Metadata metadata = Metadata.builderWithMap(["profile": "ProductionEnv"]).build()
        assertTrue(metadataPattern.matches(metadata))
    }

    @Test
    void test_NULLOBJECT() {
        MetadataPattern mp = MetadataPattern.NULL_OBJECT
        assertNotNull(mp)
        assertEquals(0, mp.size())
    }


    @Test
    void test_NULLOBJECT_toString() {
        String expected = "{}"
        String actual = MetadataPattern.NULL_OBJECT.toString()
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

    @Test
    void test_toSpanSequence_regex() {
        MetadataPattern pattern = MetadataPattern.builder()
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
        IgnoringMetadataKeys ignoringMetadataKeys = IgnoringMetadataKeys.NULL_OBJECT
        MetadataPattern pattern = MetadataPattern.builderWithMetadata(metadata, ignoringMetadataKeys).build()
        String expected = '''{"B":"b", "C":"c", "a":"a"}'''
        String actual = pattern.toString()
        println JsonOutput.prettyPrint(actual)
        assertEquals(expected, actual)
    }

}