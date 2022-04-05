package com.kazurayam.materialstore.filesystem.metadata;

import com.kazurayam.materialstore.util.JsonUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public class MetadataAttributeTest {

    private MetadataAttribute instance;

    @BeforeEach
    public void beforeEach() {
        String v = "foo/DevEnv_1.2.3-beta/bar";
        instance = new MetadataAttribute("profile", v);
        instance.setIgnoredByKey(true);
        instance.setIdentifiedByValue(true);
        Matcher m = new SemanticVersionPattern("foo/DevEnv_1.0.0/bar").matcher(v);
        SemanticVersionMatcherResult result = new SemanticVersionMatcherResult(m);
        instance.setSemanticVersionMatcherResult(result);
    }

    @Test
    public void test_constructor0() {
        MetadataAttribute instance0 = new MetadataAttribute("URL.path", "/");
        Assertions.assertEquals("URL.path", instance0.getKey());
        Assertions.assertEquals("/", instance0.getValue());
    }

    @Test
    public void test_constructor1() {
        MetadataAttribute instance1 = new MetadataAttribute("URL.fragment", "1234567");
        Assertions.assertEquals("URL.fragment", instance1.getKey());
        Assertions.assertEquals("1234567", instance1.getValue());
    }

    @Test
    public void test_toString() {
        Assertions.assertNotNull(instance.toString());
    }

    @Test
    public void test_toJson() {
        String json = instance.toJson(true);
        System.out.println("[test_toJson] " + json);
        Assertions.assertNotNull(json);
    }

    @Test
    public void test_isIgnoredByKey() {
        Assertions.assertTrue(instance.isIgnoredByKey());
    }

    @Test
    public void test_isIdentifiedByValue() {
        Assertions.assertTrue(instance.isIdentifiedByValue());
    }

    @Test
    public void test_getSemanticVersion() {
        SemanticVersionMatcherResult result = instance.getSemanticVersionMatcherResult();
        Assertions.assertNotNull(result);
        Assertions.assertEquals(4, result.size());
        Assertions.assertEquals("foo/DevEnv_", result.getHeader());
        Assertions.assertEquals("1.2.3-beta", result.getVersion());
        Assertions.assertEquals("/bar", result.getTrailer());
    }

    @Test
    public void test_toTemplateModel() {
        Map<String, Object> metadataAttribute = instance.toTemplateModel();
        System.out.println(JsonUtil.prettyPrint(instance.toJson()));
/*
{
    "key": "profile",
    "value": "foo/DevEnv_1.2.3-beta/bar",
    "ignoredByKey": true,
    "identifiedByValue": true,
    "semanticVersionMatcherResult": {
        "matched": true,
        "fragments": [
            "foo/DevEnv_",
            "1.2.3-beta",
            "-beta",
            "/bar"
        ]
    }
}
 */
        Assertions.assertEquals("profile", metadataAttribute.get("key"));
        Assertions.assertEquals("foo/DevEnv_1.2.3-beta/bar", metadataAttribute.get("value"));
        Assertions.assertEquals(true, metadataAttribute.get("ignoredByKey"));
        Assertions.assertEquals(true, metadataAttribute.get("identifiedByValue"));

        SemanticVersionMatcherResult svmr = instance.getSemanticVersionMatcherResult();
        Map<String, Object> result = svmr.toTemplateModel();
        Assertions.assertTrue((boolean) result.get("matched"));

        List<String> fragments = svmr.fragments();
        Assertions.assertEquals("foo/DevEnv_", fragments.get(0));
        Assertions.assertEquals("1.2.3-beta", fragments.get(1));
        Assertions.assertEquals("-beta", fragments.get(2));
        Assertions.assertEquals("/bar", fragments.get(3));
    }
}
