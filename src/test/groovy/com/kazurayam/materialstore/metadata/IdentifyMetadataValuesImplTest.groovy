package com.kazurayam.materialstore.metadata


import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertTrue

class IdentifyMetadataValuesImplTest {

    IdentifyMetadataValuesImpl imv

    @BeforeEach
    void setup() {
        imv = new IdentifyMetadataValues.Builder()
                .putAllNameRegexPairs(["URL.query": "\\w{32}"])
                .build()
    }

    @Test
    void test_matchesWithAttributeOf() {
        assertTrue(imv.matchesWithAttributeOf("URL.query", "856008caa5eb66df68595e734e59580d"))
    }

}
