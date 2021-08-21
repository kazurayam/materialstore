package com.kazurayam.materialstore

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*

class MaterialListTest {

    private final String sampleLine = """6141b40cfe9e7340a483a3097c4f6ff5d20e04ea\tpng\t{"profile":"DevelopmentEnv","URL":"http://demoaut-mimic.kazurayam.com/"}"""

    private MetadataPattern metadataPattern = MetadataPattern.ANY
    private FileType fileType = FileType.PNG
    private Material material

    @BeforeEach
    void beforeEach() {
        IndexEntry indexEntry = IndexEntry.parseLine(sampleLine)
        material = new Material(JobName.NULL_OBJECT, JobTimestamp.NULL_OBJECT, indexEntry)
    }

    @Test
    void test_smoke() {
        MaterialList materialList = new MaterialList(metadataPattern, fileType)
        materialList.add(material)
        assertEquals(1, materialList.size())
        assertTrue(materialList.contains(material))
    }

    @Test
    void test_getMetadataPattern() {
        MaterialList materialList = new MaterialList(metadataPattern, fileType)
        materialList.add(material)
        assertNotNull(materialList.getMetadataPattern())
        //println materialList.getMetadataPattern().toString()
    }

    @Test
    void test_getFileType() {
        MaterialList materialList = new MaterialList(metadataPattern, fileType)
        materialList.add(material)
        assertNotNull(materialList.getFileType())
        //println materialList.getFileType().extension
    }

    @Test
    void test_toString() {
        MaterialList materialList = new MaterialList(metadataPattern, fileType)
        materialList.add(material)
        String str = materialList.toString()
        assertNotNull(str)
        println str

    }
}
