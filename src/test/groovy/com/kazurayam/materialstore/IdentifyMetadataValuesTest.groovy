package com.kazurayam.materialstore

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.util.regex.Pattern

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

class IdentifyMetadataValuesTest {

    IdentifyMetadataValues imv

    @BeforeEach
    void setup() {
        imv = new IdentifyMetadataValues.Builder()
                .by(["URL.query": "\\w{32}"]).build()

    }

    @Test
    void test_size() {
        assertEquals(1, imv.size())
    }

    @Test
    void test_containsKey() {
        assertTrue(imv.containsKey("URL.query"))
    }

    @Test
    void test_keySet() {
        Set<String> keySet = imv.keySet();
        assertEquals("URL.query", keySet.getAt(0))
    }

    @Test
    void test_get() {
        Pattern pattern = imv.get("URL.pattern")
    }

    @Test
    void test_matches() {
        assertTrue(imv.matches("URL.query", "856008caa5eb66df68595e734e59580d"))
    }

}
