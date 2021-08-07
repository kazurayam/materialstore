package com.kazurayam.materialstore

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals

class DiffArtifactTest {

    @Test
    void test_getDescription() {
        MetadataPattern mp = new MetadataPattern([
                "URL.host": "demoaut-mimic.kazurayam.com",
                "URL.file": "/"
        ])
        DiffArtifact diffArtifact =
                new DiffArtifact.Builder(
                        Material.NULL_OBJECT,
                        Material.NULL_OBJECT).descriptor(mp).build()
        assertEquals(
                '''{"URL.file":"/", "URL.host":"demoaut-mimic.kazurayam.com"}''',
                diffArtifact.getDescription())
    }

}
