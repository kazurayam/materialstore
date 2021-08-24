package com.kazurayam.materialstore


import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.jupiter.api.Assertions.*

class DiffArtifactsTest {

    private static Path outputDir =
            Paths.get(".").resolve("build/tmp/testOutput")
                    .resolve(DiffArtifactsTest.class.getName())

    private static Path resultsDir =
            Paths.get(".").resolve("src/test/resources/fixture/sample_results")

    private DiffArtifacts diffArtifacts
    private DiffArtifact nullEntry

    @BeforeAll
    static void beforeAll() {
        if (Files.exists(outputDir)) {
            FileUtils.deleteDirectory(outputDir.toFile())
        }
        Files.createDirectories(outputDir)
    }

    @BeforeEach
    void before() {
        diffArtifacts = new DiffArtifacts()
    }

    @Test
    void test_add_size_get() {
        diffArtifacts.add(DiffArtifact.NULL_OBJECT)
        assertEquals(1, diffArtifacts.size())
        assertEquals(DiffArtifact.NULL_OBJECT, diffArtifacts.get(0))
    }

    @Test
    void test_iterator() {
        diffArtifacts.add(DiffArtifact.NULL_OBJECT)
        diffArtifacts.each { DiffArtifact it ->
            assert it == DiffArtifact.NULL_OBJECT
        }
    }

    @Test
    void test_setter_getter_LeftMaterialList() {
        diffArtifacts.setLeftMaterialList(MaterialList.NULL_OBJECT)
        MaterialList left = diffArtifacts.getLeftMaterialList()
        assertNotNull(left)
    }

    @Test
    void test_setter_getter_RightMaterialList() {
        diffArtifacts.setRightMaterialList(MaterialList.NULL_OBJECT)
        MaterialList right = diffArtifacts.getRightMaterialList()
        assertNotNull(right)
    }

    @Test
    void test_setter_getter_IgnoringMetadataKeys() {
        diffArtifacts.setIgnoringMetadataKeys(IgnoringMetadataKeys.NULL_OBJECT)
        IgnoringMetadataKeys ignoringMetadataKeys = diffArtifacts.getIgnoringMetadataKeys()
        assertNotNull(ignoringMetadataKeys)
    }

    /*
    [{"left":"{\"jobName\":\"_\",\"jobTimestamp\":\"_\",\"ID\":\"0000000000000000000000000000000000000000\",\"fileType\":\"\",\"metadata\":\"{}\"}","right":"{\"jobName\":\"_\",\"jobTimestamp\":\"_\",\"ID\":\"0000000000000000000000000000000000000000\",\"fileType\":\"\",\"metadata\":\"{}\"}","diff":"{\"jobName\":\"_\",\"jobTimestamp\":\"_\",\"ID\":\"0000000000000000000000000000000000000000\",\"fileType\":\"\",\"metadata\":\"{}\"}","descriptor":"{}","diffRatio":0.0}]
     */
    @Test
    void test_toString() {
        diffArtifacts.add(DiffArtifact.NULL_OBJECT)
        String s = diffArtifacts.toString()
        println s
        assertTrue(s.contains("left"), s)
        assertTrue(s.contains("right"), s)
        assertTrue(s.contains("diff"), s)
        assertTrue(s.contains("descriptor"), s)
        assertTrue(s.contains("diffRatio"), s)
    }

    @Test
    void test_countWarnings() {
        DiffArtifact tmp =
                new DiffArtifact.Builder(Material.NULL_OBJECT, Material.NULL_OBJECT)
                        .descriptor(MetadataPattern.NULL_OBJECT)
                        .build()
        tmp.setDiffRatio(45.0d)
        diffArtifacts.add(tmp)
        assertEquals(1, diffArtifacts.countWarnings(0.00d))
        assertEquals(0, diffArtifacts.countWarnings(45.00d))
        assertEquals(0, diffArtifacts.countWarnings(45.01d))
    }

}
