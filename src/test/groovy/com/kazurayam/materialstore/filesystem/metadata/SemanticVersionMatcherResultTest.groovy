package com.kazurayam.materialstore.filesystem.metadata

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.util.regex.Matcher

import static org.junit.jupiter.api.Assertions.assertEquals

class SemanticVersionMatcherResultTest {

    private final List<Map<String, String>> fixture = [
            [
                    "left": "https://cdn.jsdelivr.net/npm/bootstrap@5.1.0/dist/js/bootstrap.bundle.min.js",
                    "right": "https://cdn.jsdelivr.net/npm/bootstrap@5.1.3-rc1/dist/js/bootstrap.bundle.min.js"
            ],
            [
                    "left": "https://cdn.jsdelivr.net/npm/bootstrap@5.1.0",
                    "right": "https://cdn.jsdelivr.net/npm/bootstrap@5.1.3-rc1"
            ],
            [
                    "left": "aaa000bbb",
                    "right": "aaa000bbb"
            ],
            [
                    "left": "0123456789",
                    "right": "abcdefghi"
            ],

    ]

    List<SemanticVersionMatcherResult> resultList

    @BeforeEach
    void setup() {
        resultList = new ArrayList<>()
        for (Map<String, String> leftRight : fixture) {
            Matcher m = new SemanticVersionPattern(leftRight.right).matcher(leftRight.left)
            resultList.add(new SemanticVersionMatcherResult(m))
        }
    }

    @Test
    void test_matched() {
        resultList.eachWithIndex { result, index ->
            assert result.matched() || ! result.matched()
        }
    }

    @Test
    void test_fragments() {
        List<String> fragments = resultList[0].fragments()
        for (int i = 0; i < fragments.size(); i++) {
            println("[test_fragments] fragments[" + i + "] " + fragments[i])
        }
    }

    @Test
    void test_size() {
        assertEquals(4, resultList[0].size())
    }

    @Test
    void test_getHeader() {
        assertEquals("https://cdn.jsdelivr.net/npm/bootstrap@", resultList[0].getHeader())
    }


    @Test
    void test_getTrailer() {
        assertEquals("/dist/js/bootstrap.bundle.min.js", resultList[0].getTrailer())
    }

    @Test
    void test_getVersion() {
        assertEquals("5.1.0", resultList[0].getVersion())
    }

    @Test
    void test_toJson() {
        for (SemanticVersionMatcherResult result : resultList) {
            println "[test_toJson] " + result.toJson(true)
        }
    }
}

