package com.kazurayam.materialstore.filesystem


import com.kazurayam.materialstore.filesystem.metadata.IdentifyMetadataValues
import com.kazurayam.materialstore.filesystem.metadata.IgnoreMetadataKeys
import com.kazurayam.materialstore.filesystem.metadata.MetadataAttribute
import com.kazurayam.materialstore.net.data.DataURLEnabler
import com.kazurayam.materialstore.util.JsonUtil
import groovy.xml.MarkupBuilder
import groovy.json.JsonOutput
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*
import org.apache.http.NameValuePair
import java.util.stream.Collectors
import java.util.regex.Pattern

class MetadataTest {

    @Test
    void test_Builder_putAll() {
        URL url = new URL("https://baeldung.com/articles?topic=java&version=8#content")
        Map<String, String> additionalMetadata = ["summer":"warm", "winter":"cold"]
        Metadata metadata = Metadata.builder(url).putAll(additionalMetadata).build()
        assertNotNull(metadata)
        assertEquals("warm", metadata.get("summer"))
        assertEquals("cold", metadata.get("winter"))
    }

    @Test
    void test_builder_copy() {
        URL url = new URL("https://baeldung.com/articles?topic=java&version=8#content")
        Map<String, String> additionalMetadata = ["summer":"warm", "winter":"cold"]
        Metadata metadata = Metadata.builder(url).putAll(additionalMetadata).build()
        //
        Metadata copied = Metadata.builder(metadata).build()
        assertNotNull(copied)
        assertEquals("warm", copied.get("summer"))
        assertEquals("cold", copied.get("winter"))
    }

    @Test
    void test_Builder_with_URL_as_arg() {
        URL url = new URL("https://baeldung.com/articles?topic=java&version=8#content")
        Metadata metadata = Metadata.builder(url).build()
        assertNotNull(metadata)
        assertEquals("https", metadata.get("URL.protocol"))
        assertEquals("80", metadata.get("URL.port"))
        assertEquals("baeldung.com", metadata.get("URL.host"))
        assertEquals("/articles", metadata.get("URL.path"))
        assertEquals("topic=java&version=8", metadata.get("URL.query"))
        assertEquals("content", metadata.get("URL.fragment"))
        String ms = metadata.toString()
        //println ms
        assertTrue(ms.contains("URL.protocol") && ms.contains("https"))
        assertTrue(ms.contains("URL.host") && ms.contains("baeldung.com"))
        assertTrue(ms.contains("URL.path") && ms.contains("/articles"))
        assertTrue(ms.contains("URL.query") && ms.contains("topic=java&version=8"))
        assertTrue(ms.contains("URL.fragment") && ms.contains("content"))
    }

    @Test
    void test_Builder_with_URL_with_port() {
        URL url = new URL("http://127.0.0.1:3000/")
        Metadata metadata = Metadata.builder(url).build()
        assertNotNull(metadata)
        assertEquals("http", metadata.get("URL.protocol"))
        assertEquals("3000", metadata.get("URL.port"))
    }

    @Test
    void test_compareTo_equals() {
        Metadata metadata1 = Metadata.builder(["profile":"X"]).build()
        Metadata metadata2 = Metadata.builder(["profile":"X"]).build()
        assertEquals(0, metadata1 <=> metadata2)
    }

    @Test
    void test_compareTo_minus() {
        Metadata metadata1 = Metadata.builder(["profile":"X"]).build()
        Metadata metadata2 = Metadata.builder(["profile":"Y"]).build()
        assertEquals(-1, metadata1 <=> metadata2)
    }

    @Test
    void test_compareTo_plus() {
        Metadata metadata1 = Metadata.builder(["profile":"X"]).build()
        Metadata metadata2 = Metadata.builder(["profile":"A"]).build()
        assertEquals(1, metadata1 <=> metadata2)
    }

    @Test
    void test_constructor() {
        Metadata metadata = Metadata.builder(
                ["profile":"ProductionEnv"])
                .build()
        assertEquals("ProductionEnv", metadata.get("profile"))
    }

    @Test
    void test_containsKey() {
        Metadata metadata = Metadata.builder(["profile":"ProductionEnv"]).build()
        assertTrue(metadata.containsKey("profile"))
        assertFalse(metadata.containsKey("foo"))
    }

    @Test
    void test_equals() {
        Metadata m1 = Metadata.builder(["profile":"ProductionEnv"]).build()
        Metadata m2 = Metadata.builder(["profile":"ProductionEnv"]).build()
        Metadata m3 = Metadata.builder(["profile":"DevelopmentEnv"]).build()
        Metadata m4 = Metadata.builder(["foo":"bar"]).build()
        assertEquals(m1, m2)
        assertNotEquals(m1, m3)
        assertNotEquals(m1, m4)
    }

    @Test
    void test_get() {
        Metadata metadata = Metadata.builder(["profile":"ProductionEnv"]).build()
        assertEquals("ProductionEnv", metadata.get("profile"))
        assertNull(metadata.get("foo"))
    }

    @Test
    void test_getMetadataAttribute() {
        Metadata metadata = Metadata.builder(["profile":"ProductionEnv"]).build()
        MetadataAttribute profile = metadata.getMetadataAttribute("profile")
        assertEquals("ProductionEnv", profile.getValue())
    }

    @Test
    void test_isEmpty() {
        Metadata metadata = Metadata.builder([:]).build()
        assertTrue(metadata.isEmpty())
    }

    @Test
    void test_keySet() {
        Metadata metadata = Metadata.builder(["profile":"ProductionEnv"]).build()
        Set<String> keySet = metadata.keySet()
        assertEquals(1, keySet.size())
        assertTrue(keySet.contains("profile"))
    }

    @Test
    void test_parseURLQuery() {
        String query = "q=katalon&dfe=piiipfe&cxw=fcfw"
        List<NameValuePair> pairs = Metadata.parseURLQuery(query)
        assertTrue(pairs.stream()
                .filter({nvp -> nvp.getName() == "q"})
                .collect(Collectors.toList())
                .size() > 0
        )
        assertEquals(["katalon"],
                pairs.stream()
                        .filter({ nvp -> nvp.getName() == "q" })
                        .map({nvp -> nvp.getValue()})
                        .collect(Collectors.toList())
        )
    }

    @Test
    void test_size() {
        Metadata metadata = Metadata.builder(["key": "value"]).build()
        assertEquals(1, metadata.size())
    }

    @Test
    void test_toURLAsString() {
        String source = "https://docs.oracle.com/javase/7/docs/api/java/nio/file/Path.html#resolve(java.lang.String)"
        String sourceWithoutFragment = source.substring(0, source.indexOf("#"))
        URL url = new URL(source)
        Metadata metadata = Metadata.builder(url).build()
        println metadata.toURL().toString()
        assert metadata.toURL().toString() == sourceWithoutFragment
        assert metadata.toURLAsString() == source
    }

    @Test
    void test_toSpanSequence_dual_QueryOnMetadata_with_IdentifyMetadataValues() {
        URL url = new URL("https://baeldung.com/articles?topic=java&version=8#content")
        Metadata metadata = Metadata.builder(url)
                .put("profile", "ProductionEnv")
                .build()
        QueryOnMetadata leftQuery = QueryOnMetadata.builder()
                .put("profile", "ProductionEnv").build()
        QueryOnMetadata rightQuery = QueryOnMetadata.builder()
                .put("URL.host", "baeldung.com").build()
        IgnoreMetadataKeys ignoreMetadataKeys = IgnoreMetadataKeys.NULL_OBJECT
        IdentifyMetadataValues identifyMetadataValues =
                new IdentifyMetadataValues.Builder()
                        .putAllNameRegexPairs(["URL.query": "topic=java&version=8"])
                        .build()
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
    void test_toSpanSequence_dual_QueryOnMetadata_with_IgnoreMetadataKeys() {
        URL url = new URL("https://baeldung.com/articles?topic=java&version=8#content")
        Metadata metadata = Metadata.builder(url)
                .put("profile", "ProductionEnv")
                .build()
        QueryOnMetadata leftQuery = QueryOnMetadata.builder()
                .put("profile", "ProductionEnv").build()
        QueryOnMetadata rightQuery = QueryOnMetadata.builder()
                .put("URL.host", "baeldung.com").build()
        IgnoreMetadataKeys ignoreMetadataKeys =
                new IgnoreMetadataKeys.Builder()
                        .ignoreKey("URL.protocol")
                        .build()
        IdentifyMetadataValues identifyMetadataValues = IdentifyMetadataValues.NULL_OBJECT
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

    @Test
    void test_toSpanSequence_single_QueryOnMetadata() {
        URL url = new URL("https://baeldung.com/articles?topic=java&version=8#content")
        Metadata metadata = Metadata.builder(url)
                .put("profile", "ProductionEnv")
                .build()
        QueryOnMetadata query = QueryOnMetadata.builder()
                .put("*", Pattern.compile(".*Env"))
                .build()
        StringWriter sw = new StringWriter()
        MarkupBuilder mb = new MarkupBuilder(sw)
        metadata.toSpanSequence(mb, query)
        String str = sw.toString()
        assertNotNull(str)
        println str
        assertTrue(str.contains("matched-value"))
    }

    /**
     * In the string representation, the keys should be sorted
     */
    @Test
    void test_toJson() {
        Metadata metadata = Metadata.builder(["b": "B", "a": "A"]).build()
        String s = metadata.toJson()
        println s
        assertEquals(
                '''{"a":"A", "b":"B"}''',
                metadata.toJson())
    }

    @Test
    void test_toString_should_have_redundant_whitespaces() {
        Metadata metadata = Metadata.builder(["a":"A", "b":"B", "c":"C"]).build()
        String s = metadata.toString()
        println JsonOutput.prettyPrint(s)
        assertEquals(
                '''{"a":"A", "b":"B", "c":"C"}''',
                // white spaces here ^ and    ^
                metadata.toString())
    }

    @Test
    void test_toTemplateModel() {
        URL url = new URL("https://baeldung.com/articles?topic=java&version=8#content")
        Metadata metadata = Metadata.builder(url).build()
        println JsonUtil.prettyPrint(metadata.toJson())
        Map<String, Object> model = metadata.toTemplateModel()
        assertEquals("https", model.get("URL.protocol"))
        assertEquals("baeldung.com", model.get("URL.host"))
        assertEquals("80", model.get("URL.port"))
        assertEquals("/articles", model.get("URL.path"))
        assertEquals("topic=java&version=8", model.get("URL.query"))
        assertEquals("content", model.get("URL.fragment"))
        List<String> keyList = new ArrayList<>(model.keySet())
        Collections.sort(keyList)
        assertEquals(6, keyList.size())
        assertEquals(Arrays.asList(
                "URL.fragment",
                "URL.host",
                "URL.path",
                "URL.port",
                "URL.protocol",
                "URL.query",
        ), keyList)
    }

    @Test
    void test_toURL() {
        URL url = new URL("https://baeldung.com/articles?topic=java&version=8#content")
        Metadata metadata = Metadata.builder(url).build()
        assertNotNull(metadata)
        URL actual = metadata.toURL()
        URL expected = new URL("https://baeldung.com/articles?topic=java&version=8")
        assertEquals(expected, actual)
    }

    @Test
    void test_toURL_with_port() {
        URL url = new URL("http://127.0.0.1:3080/index")
        Metadata metadata = Metadata.builder(url).build()
        assertNotNull(metadata)
        URL actual = metadata.toURL()
        URL expected = new URL("http://127.0.0.1:3080/index")
        assertEquals(expected, actual)
    }

    @Test
    void test_toURL_file_scheme() {
        URL url = new URL("file:/c/users/foo/temp/bar")
        Metadata metadata = Metadata.builder(url).build()
        assertNotNull(metadata)
        URL actual = metadata.toURL()
        URL expected = new URL("file:/c/users/foo/temp/bar")
        assertEquals(expected, actual)
    }

    @Test
    void test_toURL_data_scheme() {
        DataURLEnabler.enableDataURL();
        String data = "data:text/html,%3Ch1%3EHello%2C%20World%21%3C%2Fh1%3E"
        URL url = new URL(data)
        Metadata metadata = Metadata.builder(url).build()
        assertNotNull(metadata)
        URL actual = metadata.toURL()
        URL expected = new URL(data)
        assertEquals(expected, actual)
    }


}
