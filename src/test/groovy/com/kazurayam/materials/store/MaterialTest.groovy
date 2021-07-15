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
        IndexEntry indexEntry = new IndexEntry(id, fileType, metadata)
        Material material = new Material(JobName.NULL_OBJECT, JobTimestamp.NULL_OBJECT, indexEntry)
        assertNotNull(material)
        assertEquals(id, material.getIndexEntry().getID())
        assertEquals(fileType, material.getIndexEntry().getFileType())
        assertEquals(metadata, material.getIndexEntry().getMetadata())
        //
        assertEquals(material, material)
    }

}
