package com.kazurayam.materialstore.filesystem

import com.kazurayam.materialstore.filesystem.metadata.IdentifyMetadataValues
import com.kazurayam.materialstore.filesystem.metadata.IgnoreMetadataKeys
import com.kazurayam.materialstore.filesystem.metadata.MetadataAttribute
import com.kazurayam.materialstore.net.data.DataURLEnabler
import com.kazurayam.materialstore.report.markupbuilder_templates.MetadataTemplate
import com.kazurayam.materialstore.util.JsonUtil
import groovy.json.JsonOutput
import groovy.xml.MarkupBuilder
import org.apache.http.NameValuePair
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

import java.util.regex.Pattern
import java.util.stream.Collectors

import static org.junit.jupiter.api.Assertions.*

class Metadata_annotateTest {

    private URL url
    private Metadata metadata
    private QueryOnMetadata query
    private IgnoreMetadataKeys ignoreMetadataKeys
    private IdentifyMetadataValues identifyMetadataValues

    @BeforeEach
    void setup() {
        url = new URL("https://baeldung.com/articles/1.0.0-beta?topic=java&version=8#content")
        metadata = Metadata.builder(url).put("profile", "ProductionEnv").build()
        query = QueryOnMetadata.builder()
                .put("*", Pattern.compile(".*Env"))
                .put("URL.host", "baeldung.com")
                .build()
        ignoreMetadataKeys = IgnoreMetadataKeys.NULL_OBJECT
        identifyMetadataValues = IdentifyMetadataValues.NULL_OBJECT
    }


    @Test
    void test_annotate_without_arguments() {
        metadata.annotate(query)
        MetadataAttribute profileAttr = metadata.getMetadataAttribute("profile")
        assertTrue(profileAttr.isMatchedByAster())
        MetadataAttribute hostAttr = metadata.getMetadataAttribute("URL.host")
        assertTrue(hostAttr.isMatchedIndividually())
    }


    @Test
    void test_annotate_with_NULL_arguments() {
        metadata.annotate(query, ignoreMetadataKeys, identifyMetadataValues)
        //println JsonUtil.prettyPrint(metadata.toJson())
        MetadataAttribute hostAttr = metadata.getMetadataAttribute("URL.host")
        assertTrue(hostAttr.isPaired())
        println "[test_annotate_with_NULL_arguments] metadata=" + metadata.toJson(true)
    }


    @Test
    void test_annotate_with_IgnoreMetadataKeys() {
        IgnoreMetadataKeys ignoreMetadataKeys =
                new IgnoreMetadataKeys.Builder()
                        .ignoreKey("URL.protocol").build()
        metadata.annotate(query, ignoreMetadataKeys, identifyMetadataValues)
        MetadataAttribute attr = metadata.getMetadataAttribute("URL.protocol")
        assertTrue(attr.isIgnoredByKey())
        println "[test_annotate_with_IgnoreMetadataKeys] metadata=" + metadata.toJson(true)
    }

    @Test
    void test_annotate_with_IdentifyMetadataValues() {
        IdentifyMetadataValues identifyMetadataValues =
                new IdentifyMetadataValues.Builder()
                        .putAllNameRegexPairs(["URL.query": "topic=java&version=8"]).build()
        metadata.annotate(query, ignoreMetadataKeys, identifyMetadataValues)
        MetadataAttribute queryAttr = metadata.getMetadataAttribute("URL.query")
        assertTrue(queryAttr.isIdentifiedByValue())
        println "[test_annotate_with_IdentifyMetadataValues] metadata=" + metadata.toJson(true)
    }

    @Disabled
    @Test
    void test_toSpanSequence_single_QueryOnMetadata() {
        QueryOnMetadata query =
                QueryOnMetadata.builder().put("*", Pattern.compile(".*Env")).build()
        //
        StringWriter sw = new StringWriter()
        MarkupBuilder mb = new MarkupBuilder(sw)
        new MetadataTemplate(metadata).toSpanSequence(mb, query)
        String str = sw.toString()
        assertNotNull(str)
        //println str
        assertTrue(str.contains("matched-value"))
    }

    @Disabled
    @Test
    void test_toSpanSequence_dual_QueryOnMetadata_with_IdentifyMetadataValues() {
        IdentifyMetadataValues identifyMetadataValues =
                new IdentifyMetadataValues.Builder()
                        .putAllNameRegexPairs(["URL.query": "topic=java&version=8"]).build()
        //
        StringWriter sw = new StringWriter()
        MarkupBuilder mb = new MarkupBuilder(sw)
        new MetadataTemplate(metadata).toSpanSequence(mb, leftQuery, rightQuery,
                ignoreMetadataKeys, identifyMetadataValues)
        String str = sw.toString()
        assertNotNull(str)
        //println str
        assertTrue(str.contains("matched-value"))
        assertTrue(str.contains("identified-value"))
    }


    @Disabled
    @Test
    void test_toSpanSequence_dual_QueryOnMetadata_with_IgnoreMetadataKeys() {
        IgnoreMetadataKeys ignoreMetadataKeys =
                new IgnoreMetadataKeys.Builder()
                        .ignoreKey("URL.protocol").build()
        //
        StringWriter sw = new StringWriter()
        MarkupBuilder mb = new MarkupBuilder(sw)
        new MetadataTemplate(metadata).toSpanSequence(mb, leftQuery, rightQuery,
                ignoreMetadataKeys, identifyMetadataValues)
        String str = sw.toString()
        assertNotNull(str)
        //println str
        assertTrue(str.contains("matched-value"))
        assertTrue(str.contains("ignored-key"))
    }
}
