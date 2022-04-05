package com.kazurayam.materialstore.filesystem.metadata;

import com.kazurayam.materialstore.filesystem.Metadata;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Set;
import java.util.regex.Pattern;

public class IdentifyMetadataValuesTest {

    private IdentifyMetadataValues imv;

    @BeforeEach
    public void setup() {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>(1);
        map.put("URL.query", "\\w{32}");
        imv = new IdentifyMetadataValues.Builder().putAllNameRegexPairs(map).build();
    }

    @Test
    public void test_size() {
        Assertions.assertEquals(1, imv.size());
    }

    @Test
    public void test_containsKey() {
        Assertions.assertTrue(imv.containsKey("URL.query"));
    }

    @Test
    public void test_keySet() {
        Set<String> keySet = imv.keySet();
        Assertions.assertTrue(keySet.contains("URL.query"));
    }

    @Test
    public void test_get() {
        Pattern pattern = imv.getPattern("URL.query");
    }

    @Test
    public void test_matches_truthy() {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>(1);
        map.put("URL.query", "856008caa5eb66df68595e734e59580d");
        Metadata metadata = Metadata.builder(map).build();
        Assertions.assertTrue(imv.matches(metadata));
    }

    @Test
    public void test_matches_falsy_no_key() {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>(1);
        map.put("foo", "bar");
        Metadata metadata = Metadata.builder(map).build();
        Assertions.assertFalse(imv.matches(metadata));
    }

    @Test
    public void test_matches_falsy_unmatching_value() {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>(1);
        map.put("URL.query", "foo");
        Metadata metadata = Metadata.builder(map).build();
        Assertions.assertFalse(imv.matches(metadata));
    }

}
