package com.kazurayam.materialstore.filesystem;

import com.kazurayam.materialstore.filesystem.metadata.IdentifyMetadataValues;
import com.kazurayam.materialstore.filesystem.metadata.IgnoreMetadataKeys;
import com.kazurayam.materialstore.filesystem.metadata.MetadataAttribute;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

public class Metadata_annotateTest {

    private Metadata metadata;
    private QueryOnMetadata query;
    private IgnoreMetadataKeys ignoreMetadataKeys;
    private IdentifyMetadataValues identifyMetadataValues;

    @BeforeEach
    public void setup() throws MalformedURLException {
        URL url = new URL("https://baeldung.com/articles/1.0.0-beta?topic=java&version=8#content");
        metadata = Metadata.builder(url).put("profile", "ProductionEnv").build();
        query = QueryOnMetadata.builder().put("*", Pattern.compile(".*Env")).put("URL.host", "baeldung.com").build();
        ignoreMetadataKeys = IgnoreMetadataKeys.NULL_OBJECT;
        identifyMetadataValues = IdentifyMetadataValues.NULL_OBJECT;
    }

    @Test
    public void test_annotate_without_arguments() {
        metadata.annotate(query);
        MetadataAttribute profileAttr = metadata.getMetadataAttribute("profile");
        Assertions.assertTrue(profileAttr.isMatchedByAster());
        MetadataAttribute hostAttr = metadata.getMetadataAttribute("URL.host");
        Assertions.assertTrue(hostAttr.isMatchedIndividually());
    }

    @Test
    public void test_annotate_with_NULL_arguments() {
        metadata.annotate(query, ignoreMetadataKeys, identifyMetadataValues);
        //println JsonUtil.prettyPrint(metadata.toJson())
        MetadataAttribute hostAttr = metadata.getMetadataAttribute("URL.host");
        Assertions.assertTrue(hostAttr.isPaired());
        System.out.println("[test_annotate_with_NULL_arguments] metadata=" + metadata.toJson(true));
    }

    @Test
    public void test_annotate_with_IgnoreMetadataKeys() {
        IgnoreMetadataKeys ignoreMetadataKeys = new IgnoreMetadataKeys.Builder().ignoreKey("URL.protocol").build();
        metadata.annotate(query, ignoreMetadataKeys, identifyMetadataValues);
        MetadataAttribute attr = metadata.getMetadataAttribute("URL.protocol");
        Assertions.assertTrue(attr.isIgnoredByKey());
        System.out.println("[test_annotate_with_IgnoreMetadataKeys] metadata=" + metadata.toJson(true));
    }

    @Test
    public void test_annotate_with_IdentifyMetadataValues() {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>(1);
        map.put("URL.query", "topic=java&version=8");
        IdentifyMetadataValues identifyMetadataValues = new IdentifyMetadataValues.Builder().putAllNameRegexPairs(map).build();
        metadata.annotate(query, ignoreMetadataKeys, identifyMetadataValues);
        MetadataAttribute queryAttr = metadata.getMetadataAttribute("URL.query");
        Assertions.assertTrue(queryAttr.isIdentifiedByValue());
        System.out.println("[test_annotate_with_IdentifyMetadataValues] metadata=" + metadata.toJson(true));
    }

}
