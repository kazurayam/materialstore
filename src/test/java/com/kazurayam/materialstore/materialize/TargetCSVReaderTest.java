package com.kazurayam.materialstore.materialize;

import com.kazurayam.materialstore.MaterialstoreException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public void test_parse_string() throws MaterialstoreException {
        String csv = "http://example.com/";
        List<Target> targetList = new TargetCSVReader().parse(csv);
        assertEquals(1, targetList.size());
    }

    @Test
    public void test_parse_reader() throws MaterialstoreException {
        Reader reader = new StringReader(csvLines);
        List<Target> targetList = new TargetCSVReader().parse(reader);
        assertEquals(2, targetList.size());
        targetList.stream()
                .map(t -> t.toJson())
                .forEach(System.out::println);
    }

    @Test
    public void test_parse_then_append_attributes() throws MaterialstoreException {
        Reader reader = new StringReader(csvLines);
        final Map<String, String> attributes = new LinkedHashMap<String, String>() {{
            put("profile", "Development");
        }};
        List<Target> targetList =
                new TargetCSVReader().parse(reader).stream()
                        .map(t -> {
                            return t.copyWith(attributes);
                        })
                        .collect(Collectors.toList());
        assertEquals(2, targetList.size());
        targetList.stream()
                .map(t -> t.toJson(true))
                .forEach(System.out::println);
    }

    @Test
    public void test_assigning_By_later() throws MaterialstoreException {
        String csv = "http://example.com/";
        List<Target> targetList =
                new TargetCSVReader().parse(csv).stream()
                        .map(t -> {
                            return t.copyWith(By.xpath("//h1[text()=\"Example Domain\"]"));
                        })
                        .collect(Collectors.toList());
        assertEquals(1, targetList.size());
        assertEquals("By.xpath: //h1[text()=\"Example Domain\"]",
                targetList.get(0).getBy().toString());
    }
}
