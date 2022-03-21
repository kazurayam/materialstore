package com.kazurayam.materialstore.filesystem


import com.kazurayam.materialstore.filesystem.metadata.IdentifyMetadataValues
import com.kazurayam.materialstore.filesystem.metadata.IgnoreMetadataKeys
import com.kazurayam.materialstore.filesystem.metadata.MetadataAttribute
import com.kazurayam.materialstore.net.data.DataURLEnabler
import com.kazurayam.materialstore.util.JsonUtil
import groovy.xml.MarkupBuilder
import groovy.json.JsonOutput
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*
import org.apache.http.NameValuePair
import java.util.stream.Collectors
import java.util.regex.Pattern

class MetadataTest {

    static String url0 = "https://cdnjs.cloudflare.com/ajax/libs/jquery/1.11.3/jquery.js?q=12345"
    static String url1 = "https://cdnjs.cloudflare.com/ajax/libs/jquery/1.12.4/jquery.js?q=67890"
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
    void test_canBeIdentified() {
        IdentifyMetadataValues identifyMetadataValues =
                new IdentifyMetadataValues.Builder()
                        .putAllNameRegexPairs(["profile": "MyAdmin_ProductionEnv"])
                        .build()
        assertTrue(metadata0.canBeIdentified("profile", identifyMetadataValues))
    }

    @Test
    void test_canBePaired() {
        QueryOnMetadata query0 = QueryOnMetadata.builder(metadata0).build()
        assertTrue(metadata0.canBePaired(query0, "URL.host"))
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
    void test_matchesByAster() {
        Metadata metadata = Metadata.builder(["profile":"ProductionEnv"]).build()
        QueryOnMetadata query = QueryOnMetadata.builder(metadata)
                .put("*", "ProductionEnv").build()
        assertTrue(metadata.matchesByAster(query, "profile"))
    }

    @Test
    void test_matchesIndividually() {
        Metadata metadata = Metadata.builder(["profile":"ProductionEnv"]).build()
        QueryOnMetadata query = QueryOnMetadata.builder(metadata).build()
        assertTrue(metadata.matchesIndividually(query, "profile"))
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


    /**
     * In the string representation, the keys should be sorted
     */
    @Test
    void test_toSimplifiedJson() {
        Metadata metadata = Metadata.builder(["b": "B", "a": "A"]).build()
        String s = metadata.toSimplifiedJson()
        println s
        assertEquals(
                '''{"a":"A", "b":"B"}''',
                metadata.toSimplifiedJson())
    }

    @Test
    void test_toSimplifiedJson_should_have_redundant_whitespaces() {
        Metadata metadata = Metadata.builder(["a":"A", "b":"B", "c":"C"]).build()
        String s = metadata.toSimplifiedJson()
        println JsonOutput.prettyPrint(s)
        assertEquals(
                '''{"a":"A", "b":"B", "c":"C"}''',
                // white spaces here ^ and    ^
                metadata.toSimplifiedJson())
    }

    @Test
    void test_toTemplateModel() {
        URL url = new URL("https://baeldung.com/articles?topic=java&version=8#content")
        Metadata metadata = Metadata.builder(url).build()
        println JsonUtil.prettyPrint(metadata.toJson())
        Map<String, Object> model = metadata.toTemplateModel()
        assertEquals("https",
                ((Map)model.get("URL.protocol")).get("value"))
        assertEquals("baeldung.com",
                ((Map)model.get("URL.host")).get("value"))
        assertEquals("80",
                ((Map)model.get("URL.port")).get("value"))
        assertEquals("/articles",
                ((Map)model.get("URL.path")).get("value"))
        assertEquals("topic=java&version=8",
                ((Map)model.get("URL.query")).get("value"))
        assertEquals("content",
                ((Map)model.get("URL.fragment")).get("value"))
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
