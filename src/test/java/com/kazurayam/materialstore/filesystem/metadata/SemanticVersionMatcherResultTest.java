package com.kazurayam.materialstore.filesystem.metadata;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public class SemanticVersionMatcherResultTest {

    private static final List<LinkedHashMap<String, String>> fixture;
    static {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(2);
        map.put("left", "https://cdn.jsdelivr.net/npm/bootstrap@5.1.0/dist/js/bootstrap.bundle.min.js");
        map.put("right", "https://cdn.jsdelivr.net/npm/bootstrap@5.1.3-rc1/dist/js/bootstrap.bundle.min.js");
        LinkedHashMap<String, String> map1 = new LinkedHashMap<>(2);
        map1.put("left", "https://cdn.jsdelivr.net/npm/bootstrap@5.1.0");
        map1.put("right", "https://cdn.jsdelivr.net/npm/bootstrap@5.1.3-rc1");
        LinkedHashMap<String, String> map2 = new LinkedHashMap<>(2);
        map2.put("left", "aaa000bbb");
        map2.put("right", "aaa000bbb");
        LinkedHashMap<String, String> map3 = new LinkedHashMap<>(2);
        map3.put("left", "0123456789");
        map3.put("right", "abcdefghi");
        fixture = new ArrayList<>(Arrays.asList(map, map1, map2, map3));
    }

    private List<SemanticVersionMatcherResult> resultList;

    @BeforeEach
    public void setup() {
        resultList = new ArrayList<>();
        for (Map<String, String> leftRight : fixture) {
            Matcher m =
                    new SemanticVersionPattern(leftRight.get("right"))
                            .matcher(leftRight.get("left"));
            resultList.add(new SemanticVersionMatcherResult(m));
        }
    }

    @Test
    public void test_matched() {
        resultList.forEach( result -> {
            assert result.matched() || !result.matched();
        });
    }

    @Test
    public void test_fragments() {
        List<String> fragments = resultList.get(0).fragments();
        for (int i = 0; i < fragments.size() ; i++){
            System.out.println("[test_fragments] fragments[" + i + "] " + fragments.get(i));
        }

    }

    @Test
    public void test_size() {
        Assertions.assertEquals(4, resultList.get(0).size());
    }

    @Test
    public void test_getHeader() {
        Assertions.assertEquals("https://cdn.jsdelivr.net/npm/bootstrap@", resultList.get(0).getHeader());
    }

    @Test
    public void test_getTrailer() {
        Assertions.assertEquals("/dist/js/bootstrap.bundle.min.js", resultList.get(0).getTrailer());
    }

    @Test
    public void test_getVersion() {
        Assertions.assertEquals("5.1.0", resultList.get(0).getVersion());
    }

    @Test
    public void test_toJson() {
        for (SemanticVersionMatcherResult result : resultList) {
            System.out.println("[test_toJson] " + result.toJson(true));
        }

    }

}
