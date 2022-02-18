package com.kazurayam.materialstore.diffartifact

import com.kazurayam.materialstore.filesystem.Material
import com.kazurayam.materialstore.filesystem.MaterialList
import com.kazurayam.materialstore.metadata.IdentifyMetadataValues
import com.kazurayam.materialstore.metadata.IgnoringMetadataKeys
import com.kazurayam.materialstore.metadata.MetadataPattern
import groovy.json.JsonOutput
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.jupiter.api.Assertions.*

class DiffArtifactGroupTest {

    private static Path outputDir =
            Paths.get(".").resolve("build/tmp/testOutput")
                    .resolve(DiffArtifactGroupTest.class.getName())

    private static Path resultsDir =
            Paths.get(".").resolve("src/test/resources/fixture/sample_results")

    private DiffArtifactGroup diffArtifactGroup
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
        diffArtifactGroup = new DiffArtifactGroup()
    }

    @Test
    void test_add_size_get() {
        diffArtifactGroup.add(DiffArtifact.NULL_OBJECT)
        assertEquals(1, diffArtifactGroup.size())
        assertEquals(DiffArtifact.NULL_OBJECT, diffArtifactGroup.get(0))
    }

    @Test
    void test_countWarnings() {
        DiffArtifact tmp =
                new DiffArtifact.Builder(Material.NULL_OBJECT, Material.NULL_OBJECT)
                        .setMetadataPattern(MetadataPattern.NULL_OBJECT)
                        .build()
        tmp.setDiffRatio(45.0d)
        diffArtifactGroup.add(tmp)
        assertEquals(1, diffArtifactGroup.countWarnings(0.00d))
        assertEquals(0, diffArtifactGroup.countWarnings(45.00d))
        assertEquals(0, diffArtifactGroup.countWarnings(45.01d))
    }

    @Test
    void test_iterator() {
        diffArtifactGroup.add(DiffArtifact.NULL_OBJECT)
        diffArtifactGroup.each { DiffArtifact it ->
            assert it == DiffArtifact.NULL_OBJECT
        }
    }

    @Test
    void test_setter_getter_IdentifyMetadataValues() {
        IdentifyMetadataValues imv = IdentifyMetadataValues.by(["URL.query":"\\w{32}"])
        diffArtifactGroup.setIdentifyMetadataValues(imv)
        IdentifyMetadataValues result = diffArtifactGroup.getIdentifyMetadataValues()
        assertEquals(imv, result)
    }

    @Test
    void test_setter_getter_IgnoringMetadataKeys() {
        diffArtifactGroup.setIgnoringMetadataKeys(IgnoringMetadataKeys.NULL_OBJECT)
        IgnoringMetadataKeys ignoringMetadataKeys = diffArtifactGroup.getIgnoringMetadataKeys()
        assertNotNull(ignoringMetadataKeys)
    }

    @Test
    void test_setter_getter_LeftMaterialList() {
        diffArtifactGroup.setLeftMaterialList(MaterialList.NULL_OBJECT)
        MaterialList left = diffArtifactGroup.getLeftMaterialList()
        assertNotNull(left)
    }

    @Test
    void test_setter_getter_RightMaterialList() {
        diffArtifactGroup.setRightMaterialList(MaterialList.NULL_OBJECT)
        MaterialList right = diffArtifactGroup.getRightMaterialList()
        assertNotNull(right)
    }

    @Test
    void test_toString() {
        diffArtifactGroup.add(DiffArtifact.NULL_OBJECT)
        String s = diffArtifactGroup.toString()
        println JsonOutput.prettyPrint(s)
        assertTrue(s.contains("left"), s)
        assertTrue(s.contains("right"), s)
        assertTrue(s.contains("diff"), s)
        assertTrue(s.contains("metadataPattern"), s)
        assertTrue(s.contains("diffRatio"), s)
    }



}
