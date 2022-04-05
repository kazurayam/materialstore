package com.kazurayam.materialstore.filesystem;

import com.kazurayam.materialstore.filesystem.metadata.IgnoreMetadataKeys;
import com.kazurayam.materialstore.filesystem.metadata.SortKeys;
import com.kazurayam.materialstore.util.JsonUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class QueryOnMetadataTest {
    @Test
    public void test_ANY() {
        QueryOnMetadata query = QueryOnMetadata.ANY;
        Assertions.assertNotNull(query);
        Assertions.assertEquals(1, query.size());
        Assertions.assertTrue(query.containsKey("*"));
        Assertions.assertEquals("re:.*", query.getAsString("*"));
    }

    @Test
    public void test_ANY_toString() {
        String expected = "{\"*\":\"re:.*\"}";
        String actual = QueryOnMetadata.ANY.toString();
        Assertions.assertEquals(expected, actual);
    }


    @Test
    public void test_create_with_IgnoreMetadataKeys() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(2);
        map.put("profile", "ProjectionEnv");
        map.put("category", "screenshot");
        Metadata metadata = Metadata.builder(map).build();
        IgnoreMetadataKeys ignoreMetadataKeys = new IgnoreMetadataKeys.Builder().ignoreKey("profile").build();
        QueryOnMetadata query = QueryOnMetadata.builder(metadata, ignoreMetadataKeys).build();
        Assertions.assertNotNull(query);
        Assertions.assertFalse(query.containsKey("profile"));
        Assertions.assertTrue(query.containsKey("category"));
    }

    @Test
    public void test_create_without_IgnoreMetadataKeys() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(2);
        map.put("profile", "ProjectionEnv");
        map.put("category", "screenshot");
        Metadata metadata = Metadata.builder(map).build();
        QueryOnMetadata query = QueryOnMetadata.builder(metadata).build();
        Assertions.assertNotNull(query);
        Assertions.assertTrue(query.containsKey("profile"));
        Assertions.assertTrue(query.containsKey("category"));
    }

    @Test
    public void test_getDescription() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(3);
        map.put("URL.path", "/");
        map.put("profile", "Flaskr_ProductionEnv");
        map.put("step", "6");
        QueryOnMetadata mp = QueryOnMetadata.builder(map).build();
        SortKeys sortKeys = new SortKeys("step", "profile");
        String description = mp.getDescription(sortKeys);
        Assertions.assertEquals("{\"step\":\"6\", \"profile\":\"Flaskr_ProductionEnv\", \"URL.path\":\"/\"}", description);
    }

    @Test
    public void test_matches_ANY() {
        QueryOnMetadata query = QueryOnMetadata.ANY;
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("profile", "ProductionEnv");
        Metadata metadata = Metadata.builder(map).build();
        Assertions.assertTrue(query.matches(metadata));
    }

    @Test
    public void test_matches_falsy() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("profile", "ProductionEnv");
        QueryOnMetadata query = QueryOnMetadata.builder(map).build();
        LinkedHashMap<String, String> map1 = new LinkedHashMap<>(1);
        map1.put("foo", "bar");
        Metadata metadata = Metadata.builder(map1).build();
        Assertions.assertFalse(query.matches(metadata));
    }

    @Test
    public void test_matches_multiple_AND_conditions() {
        QueryOnMetadata query = QueryOnMetadata.builder().put("profile", "ProductionEnv").put("URL.file", Pattern.compile(".*")).build();
        //
        LinkedHashMap<String, String> map = new LinkedHashMap<>(2);
        map.put("profile", "ProductionEnv");
        map.put("URL.file", "/");
        Metadata metadata1 = Metadata.builder(map).build();
        Assertions.assertTrue(query.matches(metadata1));
        //
        LinkedHashMap<String, String> map1 = new LinkedHashMap<>(2);
        map1.put("profile", "DevelopEnv");
        map1.put("URL.file", "/");
        Metadata metadata2 = Metadata.builder(map1).build();
        Assertions.assertFalse(query.matches(metadata2));
        //
        LinkedHashMap<String, String> map2 = new LinkedHashMap<>(1);
        map2.put("profile", "ProductionEnv");
        Metadata metadata3 = Metadata.builder(map2).build();
        Assertions.assertFalse(query.matches(metadata3));
        //
        LinkedHashMap<String, String> map3 = new LinkedHashMap<>(1);
        map3.put("URL.file", "/");
        Metadata metadata4 = Metadata.builder(map3).build();
        Assertions.assertFalse(query.matches(metadata4));
    }

    @Test
    public void test_matches_2_paths_with_semantic_version() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(2);
        map.put("profile", "prod");
        map.put("URL.path", "/npm/bootstrap@5.1.0/dist/js/bootstrap.bundle.min.js");
        Metadata metadataLeft = Metadata.builder(map).build();
        LinkedHashMap<String, String> map1 = new LinkedHashMap<>(2);
        map1.put("profile", "dev");
        map1.put("URL.path", "/npm/bootstrap@5.1.3-alpha/dist/js/bootstrap.bundle.min.js");
        Metadata metadataRight = Metadata.builder(map1).build();
        IgnoreMetadataKeys ignoreMetadataKeys = new IgnoreMetadataKeys.Builder().ignoreKey("profile").build();
        QueryOnMetadata query = QueryOnMetadata.builder(metadataRight, ignoreMetadataKeys).build();
        assert query.matches(metadataLeft);
    }

    @Test
    public void test_matches_truthy() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("profile", "ProductionEnv");
        QueryOnMetadata query = QueryOnMetadata.builder(map).build();
        LinkedHashMap<String, String> map1 = new LinkedHashMap<>(1);
        map1.put("profile", "ProductionEnv");
        Metadata metadata = Metadata.builder(map1).build();
        Assertions.assertTrue(query.matches(metadata));
    }

    @Test
    public void test_NULLOBJECT() {
        QueryOnMetadata query = QueryOnMetadata.NULL_OBJECT;
        Assertions.assertNotNull(query);
        Assertions.assertEquals(0, query.size());
    }

    @Test
    public void test_NULLOBJECT_toString() {
        String expected = "{}";
        String actual = QueryOnMetadata.NULL_OBJECT.toString();
        Assertions.assertEquals(expected, actual);
    }

    private static QueryOnMetadata getToSpanSequenceFixture() {
        Metadata metadata = Metadata.builder()
                .put("profile", "DevEnv")
                .put("URL.host", "demoaut-mimic.kazurayam.com")
                .build();
        return QueryOnMetadata.builder(metadata).put("*", Pattern.compile(".*")).build();
    }

    @Test
    public void test_toJSONTextTokens() {
        QueryOnMetadata query = getToSpanSequenceFixture();
        List<Map<String, String>> tokens = query.toJSONTextTokens();
        Assertions.assertNotNull(tokens);
        //println markup

    }

    @Test
    public void test_toTemplateModel() {
        QueryOnMetadata query = getToSpanSequenceFixture();
        //println query.toJson()
        Map<String, Object> model = query.toTemplateModel();
        Assertions.assertEquals("re:.*", model.get("*"));
        Assertions.assertEquals("demoaut-mimic.kazurayam.com", model.get("URL.host"));
        Assertions.assertEquals("DevEnv", model.get("profile"));
    }

    @Test
    public void test_toString_keys_should_be_sorted() {
        Metadata metadata = Metadata.builder().put("a", "a").put("C", "c").put("B", "b").build();
        IgnoreMetadataKeys ignoreMetadataKeys = IgnoreMetadataKeys.NULL_OBJECT;
        QueryOnMetadata query = QueryOnMetadata.builder(metadata, ignoreMetadataKeys).build();
        String expected = "{\"B\":\"b\", \"C\":\"c\", \"a\":\"a\"}";
        String actual = query.toString();
        System.out.println(JsonUtil.prettyPrint(actual));
        Assertions.assertEquals(expected, actual);
    }

}
