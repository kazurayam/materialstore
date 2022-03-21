package com.kazurayam.materialstore.filesystem.metadata

import com.kazurayam.materialstore.util.JsonUtil
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

import java.util.regex.Matcher

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertTrue

class MetadataAttributeTest {

    private MetadataAttribute instance

    @BeforeEach
    void beforeEach() {
        String v = "foo/DevEnv_1.2.3-beta/bar"
        instance = new MetadataAttribute("profile", v)
        instance.setIgnoredByKey(true)
        instance.setIdentifiedByValue(true)
        Matcher m = new SemanticVersionPattern("foo/DevEnv_1.0.0/bar").matcher(v)
        SemanticVersionMatcherResult result = new SemanticVersionMatcherResult(m)
        instance.setSemanticVersionMatcherResult(result)
    }

    @Test
    void test_constructor0() {
        MetadataAttribute instance0 = new MetadataAttribute("URL.path", "/")
        assertEquals("URL.path", instance0.getKey())
        assertEquals("/", instance0.getValue())
    }

    @Test
    void test_constructor1() {
        MetadataAttribute instance1 = new MetadataAttribute("URL.fragment", "1234567")
        assertEquals("URL.fragment", instance1.getKey())
        assertEquals("1234567", instance1.getValue())
    }

    @Test
    void test_toString() {
        assertNotNull(instance.toString())
    }

    @Test
    void test_toJson() {
        String json = instance.toJson(true)
        println "[test_toJson] " + json
        assertNotNull(json)
    }

    @Test
    void test_isIgnoredByKey() {
        assertTrue(instance.isIgnoredByKey())
    }

    @Test
    void test_isIdentifiedByValue() {
        assertTrue(instance.isIdentifiedByValue())
    }

    @Test
    void test_getSemanticVersion() {
        SemanticVersionMatcherResult result = instance.getSemanticVersionMatcherResult()
        assertNotNull(result)
        assertEquals(4, result.size())
        assertEquals("foo/DevEnv_", result.getHeader())
        assertEquals("1.2.3-beta", result.getVersion())
        assertEquals("/bar", result.getTrailer())
    }

    @Test
    void test_toTemplateModel() {
        Map<String, Object> metadataAttribute = instance.toTemplateModel()
        println JsonUtil.prettyPrint(instance.toJson())
        assertEquals("profile", metadataAttribute.get("key"))
        assertEquals("foo/DevEnv_1.2.3-beta/bar", metadataAttribute.get("value"))
        assertEquals(true, metadataAttribute.get("ignoredByKey"))
        assertEquals(true, metadataAttribute.get("identifiedByValue"))
        Map<String, Object> result = (Map<String, Object>)metadataAttribute.get("semanticVersionMatcherResult")
        assertTrue((boolean)result.get("matched"))
        /*
        "fragments": [
      "foo/DevEnv_",
      "1.2.3-beta",
      "-beta",
      "/bar"
    ]
         */
        List<String> fragments = (List<String>)result.get("fragments")
        assertEquals("foo/DevEnv_", fragments[0])
        assertEquals("1.2.3-beta", fragments[1])
        assertEquals("-beta", fragments[2])
        assertEquals("/bar", fragments[3])
    }
}
