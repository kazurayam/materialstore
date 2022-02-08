package com.kazurayam.materialstore

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.util.regex.Pattern

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertTrue

class IdentifyMetadataValuesTest {

    IdentifyMetadataValues imv

    @BeforeEach
    void setup() {
        imv = IdentifyMetadataValues.by(["URL.query": "\\w{32}"])
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
        assertEquals("URL.query", keySet[0])
    }

    @Test
    void test_get() {
        Pattern pattern = imv.get("URL.query")
    }

    @Test
    void test_matches_truthy() {
        Metadata metadata = Metadata.builderWithMap(
                ["URL.query": "856008caa5eb66df68595e734e59580d"]).build()
        assertTrue(imv.matches(metadata))
    }

    @Test
    void test_matches_falsy_no_key() {
        Metadata metadata = Metadata.builderWithMap(
                ["foo": "bar"]).build()
        assertFalse(imv.matches(metadata))
    }

    @Test
    void test_matches_falsy_unmatching_value() {
        Metadata metadata = Metadata.builderWithMap(
                ["URL.query": "foo"]).build()
        assertFalse(imv.matches(metadata))
    }

}
