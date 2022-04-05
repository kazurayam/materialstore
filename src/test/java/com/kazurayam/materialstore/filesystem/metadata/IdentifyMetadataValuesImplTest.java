package com.kazurayam.materialstore.filesystem.metadata;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

public class IdentifyMetadataValuesImplTest {

    private IdentifyMetadataValuesImpl imv;

    @BeforeEach
    public void setup() {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>(1);
        map.put("URL.query", "\\w{32}");
        imv = ((IdentifyMetadataValuesImpl) (new IdentifyMetadataValues.Builder().putAllNameRegexPairs(map).build()));
    }

    @Test
    public void test_matchesWithAttributeOf() {
        Assertions.assertTrue(imv.matchesWithAttributeOf("URL.query", "856008caa5eb66df68595e734e59580d"));
    }

}
