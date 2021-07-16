package com.kazurayam.materials.store

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertNotEquals
import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertTrue

class MetadataTest {

    @Test
    void test_constructor() {
        Metadata metadata = new Metadata(["profile":"ProductionEnv"])
        assertEquals("ProductionEnv", metadata.get("profile"))
    }

    @Test
    void test_clear() {
        Metadata metadata = new Metadata(["profile":"ProductionEnv"])
        assertEquals(1, metadata.size())
        metadata.clear()
        assertEquals(0, metadata.size())
    }

    @Test
    void test_compareTo_equals() {
        Metadata metadata1 = new Metadata(["profile":"X"])
        Metadata metadata2 = new Metadata(["profile":"X"])
        assertEquals(0, metadata1 <=> metadata2)
    }

    @Test
    void test_compareTo_minus() {
        Metadata metadata1 = new Metadata(["profile":"X"])
        Metadata metadata2 = new Metadata(["profile":"Y"])
        assertEquals(-1, metadata1 <=> metadata2)
    }

    @Test
    void test_compareTo_plus() {
        Metadata metadata1 = new Metadata(["profile":"X"])
        Metadata metadata2 = new Metadata(["profile":"A"])
        assertEquals(1, metadata1 <=> metadata2)
    }


    @Test
    void test_containsKey() {
        Metadata metadata = new Metadata(["profile":"ProductionEnv"])
        assertTrue(metadata.containsKey("profile"))
        assertFalse(metadata.containsKey("foo"))
    }

    @Test
    void test_containsValue() {
        Metadata metadata = new Metadata(["profile":"ProductionEnv"])
        assertTrue(metadata.containsValue("ProductionEnv"))
        assertFalse(metadata.containsValue("foo"))
    }

    @Test
    void test_entrySet() {
        Metadata metadata = new Metadata(["profile":"ProductionEnv"])
        Set<Map.Entry<String, String>> entrySet = metadata.entrySet()
        assertEquals(1, entrySet.size())
    }

    @Test
    void test_equals() {
        Metadata m1 = new Metadata(["profile":"ProductionEnv"])
        Metadata m2 = new Metadata(["profile":"ProductionEnv"])
        Metadata m3 = new Metadata(["profile":"DevelopmentEnv"])
        Metadata m4 = new Metadata(["foo":"bar"])
        assertEquals(m1, m2)
        assertNotEquals(m1, m3)
        assertNotEquals(m1, m4)
    }

    @Test
    void test_get() {
        Metadata metadata = new Metadata(["profile":"ProductionEnv"])
        assertEquals("ProductionEnv", metadata.get("profile"))
        assertNull(metadata.get("foo"))
    }

    @Test
    void test_isEmpty() {
        Metadata metadata = new Metadata([:])
        assertTrue(metadata.isEmpty())
        metadata.put("foo", "bar")
        assertFalse(metadata.isEmpty())
    }

    @Test
    void test_keySet() {
        Metadata metadata = new Metadata(["profile":"ProductionEnv"])
        Set<String> keySet = metadata.keySet()
        assertEquals(1, keySet.size())
        assertTrue(keySet.contains("profile"))
    }

    @Test
    void test_match_simplest() {
        Metadata target = new Metadata(["key":"value"])
        MetadataPattern pattern = new MetadataPattern(["key":"value"])
        assertTrue(target.match(pattern))
    }

    @Test
    void test_match_asterisk_pattern_matches_everything() {
        Metadata target = new Metadata(["key":"value"])
        MetadataPattern pattern = new MetadataPattern(["key":"*"])
        assertTrue(target.match(pattern))
    }

    @Test
    void test_match_demonstrative() {
        Metadata base = new Metadata([
                "profile":"ProductionEnv",
                "URL": "http://demoaut.katalon.com/",
                "URL.host": "demoaut.katalon.com/",
                "URL.file": "/"
        ])
        MetadataPattern pattern1 = new MetadataPattern([
                "profile": "*",
                "URL.file": "/"
        ])
        assertTrue(base.match(pattern1))
        MetadataPattern pattern2 = new MetadataPattern([
                "URL.file": "/"
        ])
        assertTrue(base.match(pattern2))
        MetadataPattern pattern3 = new MetadataPattern([
                "profile": "DevelopmentEnv"
        ])
        assertFalse(base.match(pattern3))
    }

    @Test
    void test_put() {
        Metadata metadata = new Metadata([:])
        metadata.put("key", "value")
        assertEquals("value", metadata.get("key"))
    }

    @Test
    void test_putAll() {
        Metadata metadata = new Metadata([:])
        metadata.putAll(["key": "value"])
        assertEquals("value", metadata.get("key"))
    }

    @Test
    void test_remove() {
        Metadata metadata = new Metadata(["key": "value"])
        metadata.remove("key")
        assertEquals(0, metadata.size())
    }

    @Test
    void test_size() {
        Metadata metadata = new Metadata(["key": "value"])
        assertEquals(1, metadata.size())
    }

    @Test
    void test_toString() {
        Metadata metadata = new Metadata(["b": "B"])
        metadata.put("a", "A")
        String s = metadata.toString()
        assertEquals(
                '''{"a":"A","b":"B"}''',
                metadata.toString())
    }

}
