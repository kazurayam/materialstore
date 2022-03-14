package com.kazurayam.materialstore.filesystem

import com.kazurayam.materialstore.filesystem.metadata.IdentifyMetadataValues
import com.kazurayam.materialstore.filesystem.metadata.IgnoreMetadataKeys
import com.kazurayam.materialstore.filesystem.metadata.MetadataAttribute
import com.kazurayam.materialstore.net.data.DataURLEnabler
import com.kazurayam.materialstore.util.JsonUtil
import groovy.json.JsonOutput
import groovy.xml.MarkupBuilder
import org.apache.http.NameValuePair
import org.junit.jupiter.api.BeforeEach
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
        leftQuery = QueryOnMetadata.builder().put("profile", "ProductionEnv").build()
        rightQuery = QueryOnMetadata.builder().put("URL.host", "baeldung.com").build()
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

    @Test
    void test_toSpanSequence_single_QueryOnMetadata() {
        QueryOnMetadata query =
                QueryOnMetadata.builder().put("*", Pattern.compile(".*Env")).build()
        //
        StringWriter sw = new StringWriter()
        MarkupBuilder mb = new MarkupBuilder(sw)
        metadata.toSpanSequence(mb, query)
        String str = sw.toString()
        assertNotNull(str)
        println str
        assertTrue(str.contains("matched-value"))
    }

    @Test
    void test_annotate_dual_QueryOnMetadata_with_IdentifyMetadataValues() {
        fail("TODO")
    }

    @Test
    void test_toSpanSequence_dual_QueryOnMetadata_with_IdentifyMetadataValues() {
        IdentifyMetadataValues identifyMetadataValues =
                new IdentifyMetadataValues.Builder()
                        .putAllNameRegexPairs(["URL.query": "topic=java&version=8"]).build()
        //
        StringWriter sw = new StringWriter()
        MarkupBuilder mb = new MarkupBuilder(sw)
        metadata.toSpanSequence(mb, leftQuery, rightQuery,
                ignoreMetadataKeys, identifyMetadataValues)
        String str = sw.toString()
        assertNotNull(str)
        println str
        assertTrue(str.contains("matched-value"))
        assertTrue(str.contains("identified-value"))
    }

    @Test
    void test_annotate_dual_QueryOnMetadata_with_IgnoreMetadataKeys() {
        fail("TODO")
    }

    @Test
    void test_toSpanSequence_dual_QueryOnMetadata_with_IgnoreMetadataKeys() {
        IgnoreMetadataKeys ignoreMetadataKeys =
                new IgnoreMetadataKeys.Builder()
                        .ignoreKey("URL.protocol").build()
        //
        StringWriter sw = new StringWriter()
        MarkupBuilder mb = new MarkupBuilder(sw)
        metadata.toSpanSequence(mb, leftQuery, rightQuery,
                ignoreMetadataKeys, identifyMetadataValues)
        String str = sw.toString()
        assertNotNull(str)
        println str
        assertTrue(str.contains("matched-value"))
        assertTrue(str.contains("ignored-key"))
    }


}
