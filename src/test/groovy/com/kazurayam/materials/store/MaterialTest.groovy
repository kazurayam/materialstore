package com.kazurayam.materials.store

import org.junit.jupiter.api.Test
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull

class MaterialTest {

    private final String sampleLine = """6141b40cfe9e7340a483a3097c4f6ff5d20e04ea\tpng\t["DevelopmentEnv","http://demoaut-mimic.kazurayam.com/"]"""

    @Test
    void test_smoke() {
        List<String> items = sampleLine.split("\t") as List<String>
        ID id = new ID(items[0])
        FileType fileType = FileType.getByExtension(items[1])
        Metadata metadata = new Metadata(items[2])
        Material material = new Material(id, fileType, metadata)
        assertNotNull(material)
        assertEquals(id, material.getID())
        assertEquals(fileType, material.getFileType())
        assertEquals(metadata, material.getMetadata())
        assertEquals(material, material)
    }

}
