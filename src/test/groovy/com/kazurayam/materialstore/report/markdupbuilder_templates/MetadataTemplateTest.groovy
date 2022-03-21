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

    /**
     * This test never passes.
     * The StyleHelper.getCSSClassName() could be wrongly designed or implemented.
     * Let me study it later.
     */
    @Test
    void test_getCSSClassName_identified_value() {
        QueryOnMetadata query = QueryOnMetadata.builder(metadata0).build()
        IdentifyMetadataValues identifyMetadataValues =
                new IdentifyMetadataValues.Builder()
                        .putAllNameRegexPairs(["profile": "MyAdmin_ProductionEnv"])
                        .build()
        String cssClassName =
                new MetadataTemplate(metadata0)
                        .getCSSClassName(query, "profile", identifyMetadataValues)
        assertEquals("identified-value", cssClassName)
    }

    @Test
    void test_getCSSClassName_matched_value() {
        QueryOnMetadata query = QueryOnMetadata.builder(metadata0).build()
        IdentifyMetadataValues identifyMetadataValues = IdentifyMetadataValues.NULL_OBJECT
        String cssClassName =
                new MetadataTemplate(metadata0)
                        .getCSSClassName(query, "URL.host", identifyMetadataValues)
        assertEquals("matched-value", cssClassName)
    }

    @Test
    void test_getCSSClassNameSolo() {
        QueryOnMetadata query = QueryOnMetadata.builder(metadata0).build()
        String cssClassName =
                new MetadataTemplate(metadata0).getCSSClassNameSolo(query, "profile")
        assertEquals("matched-value", cssClassName)
    }


    @Test
    void test_toSpanSequence_Metadata_dual_QueryOnMetadata() {
        QueryOnMetadata query = QueryOnMetadata.builder(metadata0).build()
        StringWriter sw = new StringWriter()
        MarkupBuilder mb = new MarkupBuilder(sw)
        new MetadataTemplate(metadata0).toSpanSequence(mb, query)
        String markup = sw.toString()
        //println markup
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
    }

    @Test
    void test_toSpanSequence_Metadata_single_QueryOnMetadata() {
        QueryOnMetadata query = QueryOnMetadata.builder(metadata0).build()
        IgnoreMetadataKeys ignoreMetadataKeys =
                new IgnoreMetadataKeys.Builder()
                        .ignoreKey("URL.protocol").build()
        IdentifyMetadataValues identifyMetadataValues =
                new IdentifyMetadataValues.Builder()
                        .putAllNameRegexPairs("URL.query": "q=\\d{5}").build()
        StringWriter sw = new StringWriter()
        MarkupBuilder mb = new MarkupBuilder(sw)
        new MetadataTemplate(metadata0).toSpanSequence(
                mb, query, ignoreMetadataKeys, identifyMetadataValues)
        String markup = sw.toString()
        //println markup
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
    }

}