package com.kazurayam.materialstore.report

import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.Metadata
import com.kazurayam.materialstore.filesystem.QueryOnMetadata
import com.kazurayam.materialstore.filesystem.metadata.IdentifyMetadataValues
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.jupiter.api.Assertions.*

class ReporterHelperTest {

    static String url0 = "https://cdnjs.cloudflare.com/ajax/libs/jquery/1.11.3/jquery.js"
    static String url1 = "https://cdnjs.cloudflare.com/ajax/libs/jquery/1.12.4/jquery.js"
    static String urlT = "http://myadmin.kazurayam.com/"
    static Metadata metadata0
    static Metadata metadata1
    static Metadata metadataT

    @BeforeAll
    static void beforeAll() {
        metadata0 = Metadata.builder(new URL(url0))
                .put("profile", "MyAdmin_ProductionEnv").build()
        metadata1 = Metadata.builder(new URL(url1))
                .put("profile", "MyAdmin_DevelopmentEnv").build()
        metadataT = Metadata.builder(new URL(urlT))
                .put("profile", "MyAdmin_ProductionEnv").build()

    }

    @Test
    void test_getStyleFromClasspath() {
        String style = ReporterHelper.loadStyleFromClasspath() // https://stackoverflow.com/questions/16570523/getresourceasstream-returns-null
        assertNotNull(style)
        //println style
        assertTrue(style.length() > 0)
    }

    @Test
    void test_toSpanSequence_QueryOnMetadata() {
        fail("TODO")
    }

    @Test
    void test_toSpanSequence_IgnoreMetadataKeys() {
        fail("TODO");
    }

    @Test
    void test_toSpanSequence_Metadata_single_QueryOnMetadata() {
        fail("TODO")
    }

    @Test
    void test_toSpanSequence_Metadata_dual_QueryOnMetadata() {
        fail("TODO")
    }

    @Test
    void test_getCSSClassNameSolo() {
        fail("TODO")
    }

    @Test
    void test_matchesByAster() {
        QueryOnMetadata query = QueryOnMetadata.builder().put("*", "MyAdmin_ProductionEnv").build()
        assertTrue(ReporterHelper.matchesByAster(metadata0, query, "profile"))
    }

    @Test
    void test_matchesIndividually() {
        QueryOnMetadata query = QueryOnMetadata.builder(metadata0).build()
        assertTrue(ReporterHelper.matchesIndividually(metadata0, query, "URL.host"))
    }

    @Test
    void test_getCSSClassName_matched_value() {
        QueryOnMetadata query0 = QueryOnMetadata.builder(metadata0).build()
        QueryOnMetadata query1 = QueryOnMetadata.builder(metadata1).build()
        IdentifyMetadataValues identifyMetadataValues = IdentifyMetadataValues.NULL_OBJECT
        String cssClassName =
                ReporterHelper.getCSSClassName(metadata0, query0, query1, "URL.host", identifyMetadataValues)
        assertEquals("matched-value", cssClassName)
    }

    /**
     * This test never passes.
     * The ReporterHelper.getCSSClassName() could be wrongly designed or implemented.
     * Let me study it later.
     */
    @Disabled
    @Test
    void test_getCSSClassName_identified_value() {
        QueryOnMetadata query0 = QueryOnMetadata.builder(metadata0).build()
        QueryOnMetadata queryT = QueryOnMetadata.builder(metadataT).build()
        IdentifyMetadataValues identifyMetadataValues =
                new IdentifyMetadataValues.Builder()
                        .putAllNameRegexPairs(["profile": "MyAdmin_ProductionEnv"])
                        .build()
        String cssClassName =
                ReporterHelper.getCSSClassName(metadata0, query0, queryT,
                        "profile",
                        identifyMetadataValues)
        assertEquals("identified-value", cssClassName)
    }

    @Test
    void test_canBePaired() {
        QueryOnMetadata query0 = QueryOnMetadata.builder(metadata0).build()
        QueryOnMetadata query1 = QueryOnMetadata.builder(metadata1).build()
        assertTrue(ReporterHelper.canBePaired(metadata0, query0, query1, "URL.host"))
    }

    @Test
    void test_canBeIdentified() {
        IdentifyMetadataValues identifyMetadataValues =
                new IdentifyMetadataValues.Builder()
                        .putAllNameRegexPairs(["profile": "MyAdmin_ProductionEnv"])
                        .build()
        assertTrue(ReporterHelper.canBeIdentified(metadata0,
                "profile", identifyMetadataValues))
    }
}
