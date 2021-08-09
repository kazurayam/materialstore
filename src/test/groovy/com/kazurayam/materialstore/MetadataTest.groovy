package com.kazurayam.materialstore

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*
import org.apache.http.NameValuePair
import java.util.stream.Collectors

class MetadataTest {

    @Test
    void test_constructor() {
        Metadata metadata = new MetadataImpl.Builder(["profile":"ProductionEnv"]).build()
        assertEquals("ProductionEnv", metadata.get("profile"))
    }

    @Test
    void test_compareTo_equals() {
        Metadata metadata1 = new MetadataImpl.Builder(["profile":"X"]).build()
        Metadata metadata2 = new MetadataImpl.Builder(["profile":"X"]).build()
        assertEquals(0, metadata1 <=> metadata2)
    }

    @Test
    void test_compareTo_minus() {
        Metadata metadata1 = new MetadataImpl.Builder(["profile":"X"]).build()
        Metadata metadata2 = new MetadataImpl.Builder(["profile":"Y"]).build()
        assertEquals(-1, metadata1 <=> metadata2)
    }

    @Test
    void test_compareTo_plus() {
        Metadata metadata1 = new MetadataImpl.Builder(["profile":"X"]).build()
        Metadata metadata2 = new MetadataImpl.Builder(["profile":"A"]).build()
        assertEquals(1, metadata1 <=> metadata2)
    }


    @Test
    void test_containsKey() {
        Metadata metadata = new MetadataImpl.Builder(["profile":"ProductionEnv"]).build()
        assertTrue(metadata.containsKey("profile"))
        assertFalse(metadata.containsKey("foo"))
    }

    @Test
    void test_equals() {
        Metadata m1 = new MetadataImpl.Builder(["profile":"ProductionEnv"]).build()
        Metadata m2 = new MetadataImpl.Builder(["profile":"ProductionEnv"]).build()
        Metadata m3 = new MetadataImpl.Builder(["profile":"DevelopmentEnv"]).build()
        Metadata m4 = new MetadataImpl.Builder(["foo":"bar"]).build()
        assertEquals(m1, m2)
        assertNotEquals(m1, m3)
        assertNotEquals(m1, m4)
    }

    @Test
    void test_get() {
        Metadata metadata = new MetadataImpl.Builder(["profile":"ProductionEnv"]).build()
        assertEquals("ProductionEnv", metadata.get("profile"))
        assertNull(metadata.get("foo"))
    }

    @Test
    void test_isEmpty() {
        Metadata metadata = new MetadataImpl.Builder([:]).build()
        assertTrue(metadata.isEmpty())
    }

    @Test
    void test_keySet() {
        Metadata metadata = new MetadataImpl.Builder(["profile":"ProductionEnv"]).build()
        Set<String> keySet = metadata.keySet()
        assertEquals(1, keySet.size())
        assertTrue(keySet.contains("profile"))
    }

    @Test
    void test_match_simplest() {
        Metadata target = new MetadataImpl.Builder(["key":"value"]).build()
        MetadataPattern pattern = new MetadataPattern.Builder(["key":"value"]).build()
        assertTrue(target.match(pattern))
    }

    @Test
    void test_match_asterisk_pattern_matches_everything() {
        Metadata target = new MetadataImpl.Builder(["key":"value"]).build()
        MetadataPattern pattern = new MetadataPattern.Builder(["key":"*"]).build()
        assertTrue(target.match(pattern))
    }

    @Test
    void test_match_demonstrative() {
        URL url = new URL("http://demoaut.katalon.com/")
        Metadata base = new MetadataImpl.Builder(url)
                .put("profile", "ProductionEnv")
                .build()
        //
        MetadataPattern pattern1 = new MetadataPattern.Builder(
                ["profile": "*", "URL.path": "/"]).build()
        assertTrue(base.match(pattern1))
        //
        MetadataPattern pattern2 = new MetadataPattern.Builder(
                ["URL.path": "/"]).build()
        assertTrue(base.match(pattern2))
        //
        MetadataPattern pattern3 = new MetadataPattern.Builder(
                ["profile": "DevelopmentEnv"]).build()
        assertFalse(base.match(pattern3))
    }

    @Test
    void test_size() {
        Metadata metadata = new MetadataImpl.Builder(["key": "value"]).build()
        assertEquals(1, metadata.size())
    }

    /**
     * In the string representation, the keys should be sorted
     */
    @Test
    void test_toString() {
        Metadata metadata = new MetadataImpl.Builder(["b": "B", "a": "A"]).build()
        String s = metadata.toString()
        assertEquals(
                '''{"a":"A", "b":"B"}''',
                metadata.toString())
    }

    @Test
    void test_toString_should_have_redundant_whitespaces() {
        Metadata metadata = new MetadataImpl.Builder(["a":"A", "b":"B", "c":"C"]).build()
        String s = metadata.toString()
        assertEquals(
                '''{"a":"A", "b":"B", "c":"C"}''',
                // white spaces here ^ and    ^
                metadata.toString())
    }

    @Test
    void test_parseURLQuery() {
        String query = "q=katalon&dfe=piiipfe&cxw=fcfw"
        List<NameValuePair> pairs = MetadataImpl.parseURLQuery(query)
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
    void test_URL_as_Builder_arg() {
        URL url = new URL("https://baeldung.com/articles?topic=java&version=8")
        Metadata metadata = new MetadataImpl.Builder(url).build()
        assertNotNull(metadata)
        assertEquals("baeldung.com", metadata.get("URL.host"))
        assertEquals("/articles", metadata.get("URL.path"))
        assertEquals("topic=java&version=8", metadata.get("URL.query"))
    }

    @Test
    void test_toURL() {
        URL url = new URL("https://baeldung.com/articles?topic=java&version=8")
        Metadata metadata = new MetadataImpl.Builder(url).build()
        assertNotNull(metadata)
        URL recreated = metadata.toURL()
        assertEquals(url, recreated)
    }

}
