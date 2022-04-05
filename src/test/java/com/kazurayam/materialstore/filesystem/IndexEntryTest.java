package com.kazurayam.materialstore.filesystem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kazurayam.materialstore.util.JsonUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class IndexEntryTest {

    private final String sampleLine = "6141b40cfe9e7340a483a3097c4f6ff5d20e04ea\tpng\t{\"URL\":\"http://demoaut-mimic.kazurayam.com/\", \"profile\":\"DevelopmentEnv\"}";

    @Test
    public void test_parseLine() {
        IndexEntry indexEntry = IndexEntry.parseLine(sampleLine);
        Assertions.assertEquals("6141b40", indexEntry.getShortId());
        Assertions.assertNotNull(indexEntry);
    }

    @Test
    public void test_toString() {
        IndexEntry indexEntry = IndexEntry.parseLine(sampleLine);
        String s = indexEntry.toString();
        System.out.println(JsonUtil.prettyPrint(s));
        assert !s.contains("\"{\"FileType");
        assert !s.contains("\"{\"URL");
    }

    @Test public void test_forTemplate () {
        IndexEntry indexEntry = IndexEntry.parseLine(sampleLine);
        Map<String, Object> map = indexEntry.toTemplateModel();
        // print map keys and values
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println(gson.toJson(map));
        //
        Assertions.assertTrue(((String) map.get("id")).startsWith("6141"));
        Assertions.assertTrue(map.get("fileType") instanceof Map);
        Assertions.assertTrue(map.get("metadata") instanceof Map);
    }

}
