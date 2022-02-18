package com.kazurayam.materialstore.metadata

import com.kazurayam.materialstore.metadata.IdentifyMetadataValues
import com.kazurayam.materialstore.metadata.IdentifyMetadataValuesImpl
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertTrue

class IdentifyMetadataValuesImplTest {

    IdentifyMetadataValuesImpl imv

    @BeforeEach
    void setup() {
        imv = IdentifyMetadataValues.by(["URL.query": "\\w{32}"])
    }

    @Test
    void test_matchesWithAttributeOf() {
        assertTrue(imv.matchesWithAttributeOf("URL.query", "856008caa5eb66df68595e734e59580d"))
    }

}
