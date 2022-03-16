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

class Metadata_AnnotateTest {

    private URL url
    private Metadata metadata
    private QueryOnMetadata leftQuery
    private QueryOnMetadata rightQuery
    private IgnoreMetadataKeys ignoreMetadataKeys
    private IdentifyMetadataValues identifyMetadataValues

    @BeforeEach
    void setup() {
        url = new URL("https://baeldung.com/articles?topic=java&version=8#content")
        metadata = Metadata.builder(url).put("profile", "ProductionEnv").build()
        leftQuery = QueryOnMetadata.builder()
                .put("profile", "ProductionEnv")
                .put("URL.path", "/articles")
                .build()
        rightQuery = QueryOnMetadata.builder()
                .put("URL.host", "baeldung.com")
                .put("URL.path", "/articles")
                .build()
        ignoreMetadataKeys = IgnoreMetadataKeys.NULL_OBJECT
        identifyMetadataValues = IdentifyMetadataValues.NULL_OBJECT
    }


    @Test
    void test_annotate_single_QueryOnMetadata() {
        QueryOnMetadata query =
                QueryOnMetadata.builder()
                        .put("*", Pattern.compile(".*Env"))
                        .put("URL.host", "baeldung.com")
                        .build()
        metadata.annotate(query)
        MetadataAttribute profileAttr = metadata.getMetadataAttribute("profile")
        assertTrue(profileAttr.isMatchedByAster())
        MetadataAttribute hostAttr = metadata.getMetadataAttribute("URL.host")
        assertTrue(hostAttr.isMatchedIndividually())
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
        println str
        assertTrue(str.contains("matched-value"))
    }

    @Test
    void test_annotate_dual_QueryOnMetadata() {
        metadata.annotate(leftQuery, rightQuery,
                ignoreMetadataKeys, identifyMetadataValues)
        println JsonUtil.prettyPrint(metadata.toJson())
        MetadataAttribute pathAttr = metadata.getMetadataAttribute("URL.path")
        assertTrue(pathAttr.isPaired())
        println JsonUtil.prettyPrint(pathAttr.toJson())
    }

    @Test
    void test_annotate_dual_QueryOnMetadata_with_IdentifyMetadataValues() {
        IdentifyMetadataValues identifyMetadataValues =
                new IdentifyMetadataValues.Builder()
                        .putAllNameRegexPairs(["URL.query": "topic=java&version=8"]).build()
        metadata.annotate(leftQuery, rightQuery,
                ignoreMetadataKeys, identifyMetadataValues)
        MetadataAttribute queryAttr = metadata.getMetadataAttribute("URL.query")
        assertTrue(queryAttr.isIdentifiedByValue())
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
        println str
        assertTrue(str.contains("matched-value"))
        assertTrue(str.contains("identified-value"))
    }

    @Test
    void test_annotate_dual_QueryOnMetadata_with_IgnoreMetadataKeys() {
        IgnoreMetadataKeys ignoreMetadataKeys =
                new IgnoreMetadataKeys.Builder()
                        .ignoreKey("URL.protocol").build()
        metadata.annotate(leftQuery, rightQuery,
                ignoreMetadataKeys, identifyMetadataValues)
        MetadataAttribute queryAttr = metadata.getMetadataAttribute("URL.protocol")
        assertTrue(queryAttr.isIgnoredByKey())
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
        println str
        assertTrue(str.contains("matched-value"))
        assertTrue(str.contains("ignored-key"))
    }


}
