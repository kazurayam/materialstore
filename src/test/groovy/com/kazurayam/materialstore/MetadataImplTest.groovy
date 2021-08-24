package com.kazurayam.materialstore

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*

class MetadataImplTest {

    @Test
    void test_isMatching() {
        MetadataPattern metadataPattern = MetadataPattern.builderWithMap(["profile": "ProductionEnv"]).build()
        String key = "profile"
        Metadata metadata = Metadata.builderWithMap(["profile": "ProductionEnv"]).build()
        assertTrue(MetadataImpl.isMatching(metadataPattern, key, metadata))
    }
}
