package com.kazurayam.materialstore.store


import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*

class DiffArtifactTest {

    @Test
    void test_getDescription() {
        MetadataPattern mp = new MetadataPattern([
                "URL.host": "demoaut-mimic.kazurayam.com",
                "URL.file": "/"
        ])
        DiffArtifact diffArtifact =
                new DiffArtifact(
                        Material.NULL_OBJECT,
                        Material.NULL_OBJECT,
                        mp)
        assertEquals(
                '''{"URL.file":"/","URL.host":"demoaut-mimic.kazurayam.com"}''',
                diffArtifact.getDescription())
    }

}
