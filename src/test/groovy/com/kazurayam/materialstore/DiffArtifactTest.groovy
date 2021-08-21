package com.kazurayam.materialstore

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals

class DiffArtifactTest {

    @Test
    void test_getDescription() {
        MetadataPattern mp = MetadataPattern.builderWithMap([
                "URL.host": "demoaut-mimic.kazurayam.com",
                "URL.file": "/"
        ]).build()
        DiffArtifact diffArtifact =
                new DiffArtifact.Builder(
                        Material.NULL_OBJECT,
                        Material.NULL_OBJECT).descriptor(mp).build()
        assertEquals(
                '''{"URL.file":"/", "URL.host":"demoaut-mimic.kazurayam.com"}''',
                diffArtifact.getDescription())
    }

}
