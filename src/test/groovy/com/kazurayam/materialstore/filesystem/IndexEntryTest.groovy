package com.kazurayam.materialstore.filesystem

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.junit.jupiter.api.Test
import groovy.json.JsonOutput

import static org.junit.jupiter.api.Assertions.*

class IndexEntryTest {

    private final String sampleLine =
            """6141b40cfe9e7340a483a3097c4f6ff5d20e04ea\tpng\t{"URL":"http://demoaut-mimic.kazurayam.com/", "profile":"DevelopmentEnv"}"""

    @Test
    void test_parseLine() {
        IndexEntry indexEntry = IndexEntry.parseLine(sampleLine)
        assertEquals("6141b40", indexEntry.getShortId())
        assertNotNull(indexEntry)
    }

    @Test
    void test_toString() {
        IndexEntry indexEntry = IndexEntry.parseLine(sampleLine)
        String s = indexEntry.toString()
        println JsonOutput.prettyPrint(s)
        assert ! s.contains('''"{\\"FileType''')
        assert ! s.contains('''"{\\"URL''')
    }

    @Test
    void test_forTemplate() {
        IndexEntry indexEntry = IndexEntry.parseLine(sampleLine)
        Map<String, Object> map = indexEntry.toTemplateModel();
        // print map keys and values
        Gson gson = new GsonBuilder().setPrettyPrinting().create()
        System.out.println gson.toJson(map)
        //
        assertTrue(((String)map.get("id")).startsWith("6141"))
        assertTrue(map.get("fileType") instanceof Map)
        assertTrue(map.get("metadata") instanceof Map)
    }
}
