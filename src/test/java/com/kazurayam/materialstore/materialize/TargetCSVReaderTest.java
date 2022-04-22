package com.kazurayam.materialstore.materialize;

import com.kazurayam.materialstore.MaterialstoreException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TargetCSVReaderTest {

    private static String csvLines;

    @BeforeAll
    public static void beforeAll() {
        StringBuilder sb = new StringBuilder();
        sb.append("http://www.google.com/,//input[@id=\"q\"]");
        sb.append(System.lineSeparator());
        sb.append("http://example.com/,//a[starts-with(\"More information...\")]");
        csvLines = sb.toString();
    }

    @Test
    public void test_parse() throws MaterialstoreException {
        Reader reader = new StringReader(csvLines);
        List<Target> targetList = TargetCSVReader.parse(reader);
        assertEquals(2, targetList.size());
        targetList.stream()
                .map(t -> t.toJson())
                .forEach(System.out::println);
    }

    @Test
    public void test_parse_with_attributes() throws MaterialstoreException {
        Reader reader = new StringReader(csvLines);
        Map<String, String> attributes = new LinkedHashMap<String, String>() {{
            put("profile", "Development");
        }};
        List<Target> targetList = TargetCSVReader.parse(reader, attributes);
        assertEquals(2, targetList.size());
        targetList.stream()
                .map(t -> t.toJson(true))
                .forEach(System.out::println);
    }
}
