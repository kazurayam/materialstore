package com.kazurayam.materialstore.filesystem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kazurayam.materialstore.util.JsonUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IndexEntryTest {

    private final String sampleLine = "6141b40cfe9e7340a483a3097c4f6ff5d20e04ea\tpng\t{\"URL\":\"http://demoaut-mimic.kazurayam.com/\", \"profile\":\"DevelopmentEnv\"}";

    @Test
    public void test_parseLine() {
        IndexEntry indexEntry = IndexEntry.parseLine(sampleLine);
        Assertions.assertEquals("6141b40", indexEntry.getShortId());
        Assertions.assertNotNull(indexEntry);
    }

    @Test
    public void test_isSimilarTo() {
        IndexEntry indexEntry1 = IndexEntry.parseLine(sampleLine);
        IndexEntry indexEntry2 = IndexEntry.parseLine(sampleLine.replace("6141b40", "1020304"));
        assertTrue(indexEntry1.isSimilarTo(indexEntry2));
        assertFalse(indexEntry1.equals(indexEntry2));
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
        assertTrue(((String) map.get("id")).startsWith("6141"));
        assertTrue(map.get("fileType") instanceof Map);
        assertTrue(map.get("metadata") instanceof Map);
    }

}
