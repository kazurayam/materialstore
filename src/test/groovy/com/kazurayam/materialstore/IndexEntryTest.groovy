package com.kazurayam.materialstore

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*

class IndexEntryTest {

    private final String sampleLine =
            """6141b40cfe9e7340a483a3097c4f6ff5d20e04ea\tpng\t{"URL":"http://demoaut-mimic.kazurayam.com/", "profile":"DevelopmentEnv"}"""

    @Test
    void test_parseLine() {
        IndexEntry indexEntry = IndexEntry.parseLine(sampleLine)
        assertNotNull(indexEntry)
    }
}
