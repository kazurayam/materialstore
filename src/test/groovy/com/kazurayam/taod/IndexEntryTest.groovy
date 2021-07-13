package com.kazurayam.taod

import org.junit.jupiter.api.Test
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertTrue
import static org.junit.jupiter.api.Assertions.fail

class IndexEntryTest {

    private final String sampleLine = """6141b40cfe9e7340a483a3097c4f6ff5d20e04ea\tpng\t["DevelopmentEnv","http://demoaut-mimic.kazurayam.com/"]"""

    @Test
    void test_smoke() {
        List<String> items = sampleLine.split("\t") as List<String>
        ID id = new ID(items[0])
        FileType fileType = FileType.getByExtension(items[1])
        Metadata metadata = new Metadata(items[2])
        IndexEntry indexEntry = new IndexEntry(id, fileType, metadata)
        assertNotNull(indexEntry)
        assertEquals(id, indexEntry.getID())
        assertEquals(fileType, indexEntry.getFileType())
        assertEquals(metadata, indexEntry.getMetadata())
        assertEquals(indexEntry, indexEntry)
    }

}
