package com.kazurayam.materialstore.store

import org.junit.jupiter.api.Test
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull

class MaterialTest {

    private final String sampleLine = """6141b40cfe9e7340a483a3097c4f6ff5d20e04ea\tpng\t{"profile":"DevelopmentEnv","URL":"http://demoaut-mimic.kazurayam.com/"}"""

    @Test
    void test_smoke() {
        IndexEntry indexEntry = IndexEntry.parseLine(sampleLine)
        Material material = new Material(JobName.NULL_OBJECT, JobTimestamp.NULL_OBJECT, indexEntry)
        assertNotNull(material)
        assertEquals("6141b40cfe9e7340a483a3097c4f6ff5d20e04ea",
                material.getIndexEntry().getID().toString())
        assertEquals(FileType.PNG, material.getIndexEntry().getFileType())
        assertEquals("""{"URL":"http://demoaut-mimic.kazurayam.com/","profile":"DevelopmentEnv"}""",
                material.getIndexEntry().getMetadata().toString())
        //
        assertEquals(material, material)
    }

}
