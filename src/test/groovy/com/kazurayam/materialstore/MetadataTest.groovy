package com.kazurayam.materialstore

import groovy.xml.MarkupBuilder
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*
import org.apache.http.NameValuePair
import java.util.stream.Collectors
import java.util.regex.Pattern

class MetadataTest {

    @Test
    void test_Builder_with_URL_as_arg() {
        URL url = new URL("https://baeldung.com/articles?topic=java&version=8#content")
        Metadata metadata = Metadata.builderWithUrl(url).build()
        assertNotNull(metadata)
        assertEquals("https", metadata.get("URL.protocol"))
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
    void test_Builder_putAll() {
        URL url = new URL("https://baeldung.com/articles?topic=java&version=8#content")
        Map<String, String> additionalMetadata = ["summer":"warm", "winter":"cold"]
        Metadata metadata = Metadata.builderWithUrl(url).putAll(additionalMetadata).build()
        assertNotNull(metadata)
        assertEquals("warm", metadata.get("summer"))
        assertEquals("cold", metadata.get("winter"))
    }


    @Test
    void test_constructor() {
        Metadata metadata = Metadata.builderWithMap([
                "profile":"ProductionEnv"])
                .build()
        assertEquals("ProductionEnv", metadata.get("profile"))
    }

    @Test
    void test_compareTo_equals() {
        Metadata metadata1 = Metadata.builderWithMap(["profile":"X"]).build()
        Metadata metadata2 = Metadata.builderWithMap(["profile":"X"]).build()
        assertEquals(0, metadata1 <=> metadata2)
    }

    @Test
    void test_compareTo_minus() {
        Metadata metadata1 = Metadata.builderWithMap(["profile":"X"]).build()
        Metadata metadata2 = Metadata.builderWithMap(["profile":"Y"]).build()
        assertEquals(-1, metadata1 <=> metadata2)
    }

    @Test
    void test_compareTo_plus() {
        Metadata metadata1 = Metadata.builderWithMap(["profile":"X"]).build()
        Metadata metadata2 = Metadata.builderWithMap(["profile":"A"]).build()
        assertEquals(1, metadata1 <=> metadata2)
    }


    @Test
    void test_containsKey() {
        Metadata metadata = Metadata.builderWithMap(["profile":"ProductionEnv"]).build()
        assertTrue(metadata.containsKey("profile"))
        assertFalse(metadata.containsKey("foo"))
    }

    @Test
    void test_equals() {
        Metadata m1 = Metadata.builderWithMap(["profile":"ProductionEnv"]).build()
        Metadata m2 = Metadata.builderWithMap(["profile":"ProductionEnv"]).build()
        Metadata m3 = Metadata.builderWithMap(["profile":"DevelopmentEnv"]).build()
        Metadata m4 = Metadata.builderWithMap(["foo":"bar"]).build()
        assertEquals(m1, m2)
        assertNotEquals(m1, m3)
        assertNotEquals(m1, m4)
    }

    @Test
    void test_get() {
        Metadata metadata = Metadata.builderWithMap(["profile":"ProductionEnv"]).build()
        assertEquals("ProductionEnv", metadata.get("profile"))
        assertNull(metadata.get("foo"))
    }

    @Test
    void test_isEmpty() {
        Metadata metadata = Metadata.builderWithMap([:]).build()
        assertTrue(metadata.isEmpty())
    }

    @Test
    void test_keySet() {
        Metadata metadata = Metadata.builderWithMap(["profile":"ProductionEnv"]).build()
        Set<String> keySet = metadata.keySet()
        assertEquals(1, keySet.size())
        assertTrue(keySet.contains("profile"))
    }


    @Test
    void test_size() {
        Metadata metadata = Metadata.builderWithMap(["key": "value"]).build()
        assertEquals(1, metadata.size())
    }

    /**
     * In the string representation, the keys should be sorted
     */
    @Test
    void test_toString() {
        Metadata metadata = Metadata.builderWithMap(["b": "B", "a": "A"]).build()
        String s = metadata.toString()
        assertEquals(
                '''{"a":"A", "b":"B"}''',
                metadata.toString())
    }

    @Test
    void test_toString_should_have_redundant_whitespaces() {
        Metadata metadata = Metadata.builderWithMap(["a":"A", "b":"B", "c":"C"]).build()
        String s = metadata.toString()
        assertEquals(
                '''{"a":"A", "b":"B", "c":"C"}''',
                // white spaces here ^ and    ^
                metadata.toString())
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
    void test_toURL() {
        URL url = new URL("https://baeldung.com/articles?topic=java&version=8#content")
        Metadata metadata = Metadata.builderWithUrl(url).build()
        assertNotNull(metadata)
        URL recreated = metadata.toURL()
        URL urlWithoutFragment = new URL("https://baeldung.com/articles?topic=java&version=8")
        assertEquals(urlWithoutFragment, recreated)
    }

    @Test
    void test_toSpanSequence_single_MetadataPattern() {
        URL url = new URL("https://baeldung.com/articles?topic=java&version=8#content")
        Metadata metadata = Metadata.builderWithUrl(url)
                .put("profile", "ProductionEnv")
                .build()
        MetadataPattern metadataPattern = MetadataPattern.builder()
                .put("*", Pattern.compile(".*Env"))
                .build()
        StringWriter sw = new StringWriter()
        MarkupBuilder mb = new MarkupBuilder(sw)
        metadata.toSpanSequence(mb, metadataPattern)
        String str = sw.toString()
        assertNotNull(str)
        println str
        assertTrue(str.contains("matched-value"))
    }

    @Test
    void test_toSpanSequence_dual_MetadataPatterns() {
        URL url = new URL("https://baeldung.com/articles?topic=java&version=8#content")
        Metadata metadata = Metadata.builderWithUrl(url)
                .put("profile", "ProductionEnv")
                .build()
        MetadataPattern leftMetadataPattern = MetadataPattern.builder()
                .put("profile", "ProductionEnv").build()
        MetadataPattern rightMetadataPattern = MetadataPattern.builder()
                .put("URL.host", "baeldung.com").build()
        IgnoringMetadataKeys ignoringMetadataKeys = IgnoringMetadataKeys.of("URL.protocol")
        StringWriter sw = new StringWriter()
        MarkupBuilder mb = new MarkupBuilder(sw)
        metadata.toSpanSequence(mb, leftMetadataPattern, rightMetadataPattern, ignoringMetadataKeys)
        String str = sw.toString()
        assertNotNull(str)
        println str
        assertTrue(str.contains("matched-value"))
        assertTrue(str.contains("ignoring-key"))
    }
}
