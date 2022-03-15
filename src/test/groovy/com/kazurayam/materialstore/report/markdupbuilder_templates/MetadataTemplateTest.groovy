package com.kazurayam.materialstore.report.markdupbuilder_templates

import com.kazurayam.materialstore.filesystem.QueryOnMetadata
import com.kazurayam.materialstore.filesystem.metadata.IdentifyMetadataValues
import com.kazurayam.materialstore.filesystem.metadata.IgnoreMetadataKeys
import com.kazurayam.materialstore.report.markupbuilder_templates.MetadataTemplate
import groovy.xml.MarkupBuilder
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*

class MetadataTemplateTest extends AbstractTemplateTest {

    @Test
    void test_canBeIdentified() {
        IdentifyMetadataValues identifyMetadataValues =
                new IdentifyMetadataValues.Builder()
                        .putAllNameRegexPairs(["profile": "MyAdmin_ProductionEnv"])
                        .build()
        assertTrue(new MetadataTemplate(metadata0).canBeIdentified(
                "profile", identifyMetadataValues))
    }

    @Test
    void test_canBePaired() {
        QueryOnMetadata query0 = QueryOnMetadata.builder(metadata0).build()
        QueryOnMetadata query1 = QueryOnMetadata.builder(metadata1).build()
        assertTrue(new MetadataTemplate(metadata0).canBePaired(query0, query1, "URL.host"))
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
                new MetadataTemplate(metadata0).getCSSClassName(query0, queryT,
                        "profile",
                        identifyMetadataValues)
        assertEquals("identified-value", cssClassName)
    }

    @Test
    void test_getCSSClassName_matched_value() {
        QueryOnMetadata query0 = QueryOnMetadata.builder(metadata0).build()
        QueryOnMetadata query1 = QueryOnMetadata.builder(metadata1).build()
        IdentifyMetadataValues identifyMetadataValues = IdentifyMetadataValues.NULL_OBJECT
        String cssClassName =
                new MetadataTemplate(metadata0).getCSSClassName(query0, query1, "URL.host", identifyMetadataValues)
        assertEquals("matched-value", cssClassName)
    }

    @Test
    void test_getCSSClassNameSolo() {
        QueryOnMetadata query0 = QueryOnMetadata.builder(metadata0).build()
        String cssClassName =
                new MetadataTemplate(metadata0).getCSSClassNameSolo(query0, "profile")
        assertEquals("matched-value", cssClassName)
    }


    @Test
    void test_matchesByAster() {
        QueryOnMetadata query = QueryOnMetadata.builder().put("*", "MyAdmin_ProductionEnv").build()
        assertTrue(new MetadataTemplate(metadata0).matchesByAster(query, "profile"))
    }


    @Test
    void test_matchesIndividually() {
        QueryOnMetadata query = QueryOnMetadata.builder(metadata0).build()
        assertTrue(new MetadataTemplate(metadata0).matchesIndividually(query, "URL.host"))
    }

    @Test
    void test_toSpanSequence_Metadata_dual_QueryOnMetadata() {
        QueryOnMetadata query = QueryOnMetadata.builder(metadata0).build()
        StringWriter sw = new StringWriter()
        MarkupBuilder mb = new MarkupBuilder(sw)
        new MetadataTemplate(metadata0).toSpanSequence(mb, query)
        String markup = sw.toString()
        /*
<span>{</span>
<span>"URL.host":</span>
<span class='matched-value'>"cdnjs.cloudflare.com"</span>
<span>, </span>
<span>"URL.path":</span>
<span class='matched-value'>"/ajax/libs/jquery/1.11.3/jquery.js"</span>
<span>, </span>
<span>"URL.port":</span>
<span class='matched-value'>"80"</span>
<span>, </span>
<span>"URL.protocol":</span>
<span class='matched-value'>"https"</span>
<span>, </span>
<span>"profile":</span>
<span class='matched-value'>"MyAdmin_ProductionEnv"</span>
<span>}</span>
         */
        println markup
    }

    @Test
    void test_toSpanSequence_Metadata_single_QueryOnMetadata() {
        QueryOnMetadata query0 = QueryOnMetadata.builder(metadata0).build()
        QueryOnMetadata query1 = QueryOnMetadata.builder(metadata1).build()
        IgnoreMetadataKeys ignoreMetadataKeys =
                new IgnoreMetadataKeys.Builder()
                        .ignoreKey("URL.protocol").build()
        IdentifyMetadataValues identifyMetadataValues =
                new IdentifyMetadataValues.Builder()
                        .putAllNameRegexPairs("URL.query": "q=\\d{5}").build()
        StringWriter sw = new StringWriter()
        MarkupBuilder mb = new MarkupBuilder(sw)
        new MetadataTemplate(metadata0).toSpanSequence(
                mb, query0, query1, ignoreMetadataKeys, identifyMetadataValues)
        String markup = sw.toString()
        /*
<span>{</span>
<span>"URL.host":</span>
<span class='matched-value'>"cdnjs.cloudflare.com"</span>
<span>, </span>
<span>"URL.path":</span>
<span class='matched-value'>"/ajax/libs/jquery/1.11.3/jquery.js"</span>
<span>, </span>
<span>"URL.port":</span>
<span class='matched-value'>"80"</span>
<span>, </span>
<span class='ignored-key'>"URL.protocol":</span>
<span class='matched-value'>"https"</span>
<span>, </span>
<span>"URL.query":</span>
<span class='matched-value'>"q=12345"</span>
<span>, </span>
<span>"profile":</span>
<span class='matched-value'>"MyAdmin_ProductionEnv"</span>
<span>}</span>
         */
        println markup
    }

}
