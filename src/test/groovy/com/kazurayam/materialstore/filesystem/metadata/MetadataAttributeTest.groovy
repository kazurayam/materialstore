package com.kazurayam.materialstore.filesystem.metadata

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

class MetadataAttributeTest {

    private MetadataAttribute instance

    @BeforeEach
    public void beforeEach() {
        instance = new MetadataAttribute("profile", "DevEnv_1.2.3-beta")
        instance.setIgnoredByKey(true)
        instance.setIdentifiedByValue(true)
        instance.setSemanticVersion("1.2.3-beta")
    }

    @Test
    void test_constructor0() {
        MetadataAttribute instance0 = new MetadataAttribute("URL.path")
        instance0.setValue("/")
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
        println instance.toString()
    }

    @Test
    void test_toJson() {
        println instance.toJson()
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
        assertEquals("1.2.3-beta", instance.getSemanticVersion())
    }

    @Test
    void test_toTemplateModel() {
        Map<String, Object> model = instance.toTemplateModel()
        assertEquals("profile", model.get("key"))
    }
}
