package com.kazurayam.materialstore.diffartifact

import com.kazurayam.materialstore.filesystem.ID
import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.JobTimestamp
import com.kazurayam.materialstore.filesystem.Material
import com.kazurayam.materialstore.filesystem.MaterialList
import com.kazurayam.materialstore.filesystem.Store
import com.kazurayam.materialstore.filesystem.Stores
import com.kazurayam.materialstore.metadata.IdentifyMetadataValues
import com.kazurayam.materialstore.metadata.IgnoreMetadataKeys
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
            Paths.get(".")
                    .resolve("build/tmp/testOutput")
                    .resolve(DiffArtifactGroupTest.class.getName())
    private static Path storeDir = outputDir.resolve("store")
    private static Path issue80Dir =
            Paths.get(".").resolve("src/test/resources/fixture/issue#80")


    private Store store
    private JobName jobName
    private JobTimestamp timestampP
    private JobTimestamp timestampD
    private MaterialList left
    private MaterialList right
    private DiffArtifactGroup diffArtifactGroup

    @BeforeAll
    static void beforeAll() {
        if (Files.exists(outputDir)) {
            FileUtils.deleteDirectory(outputDir.toFile())
        }
        Files.createDirectories(outputDir)
        FileUtils.copyDirectory(issue80Dir.toFile(), storeDir.toFile())
    }

    @BeforeEach
    void before() {
        MaterialList left = MaterialList.NULL_OBJECT
        MaterialList right = MaterialList.NULL_OBJECT
        diffArtifactGroup = DiffArtifactGroup.builder(left, right).build()
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
        IdentifyMetadataValues imv =
                new IdentifyMetadataValues.Builder()
                        .putAllNameRegexPairs(["URL.query":"\\w{32}"])
                        .build()
        diffArtifactGroup.setIdentifyMetadataValues(imv)
        IdentifyMetadataValues result = diffArtifactGroup.getIdentifyMetadataValues()
        assertEquals(imv, result)
    }

    @Test
    void test_setter_getter_IgnoreMetadataKeys() {
        diffArtifactGroup.setIgnoreMetadataKeys(IgnoreMetadataKeys.NULL_OBJECT)
        IgnoreMetadataKeys ignoreMetadataKeys = diffArtifactGroup.getIgnoreMetadataKeys()
        assertNotNull(ignoreMetadataKeys)
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

    void specialFixture() {
        store = Stores.newInstance(storeDir)
        jobName = new JobName("MyAdmin_visual_inspection_twins")
        timestampP = new JobTimestamp("20220128_191320")
        left = store.select(jobName, timestampP,
                MetadataPattern.builderWithMap([ "profile": "MyAdmin_ProductionEnv" ]).build()
        )
        timestampD = new JobTimestamp("20220128_191342")
        right = store.select(jobName, timestampD,
                MetadataPattern.builderWithMap(["profile": "MyAdmin_DevelopmentEnv" ]).build()
        )
    }

    @Test
    void test_Builder() {
        specialFixture()
        DiffArtifactGroup diffArtifactGroup =
                DiffArtifactGroup.builder(left, right)
                        .ignoreKeys("profile", "URL.host", "URL.port", "URL.protocol")
                        .identifyWithRegex(["URL.query":"\\w{32}"])
                        .sort("URL.path")
                        .build()
        assertNotNull(diffArtifactGroup)
        diffArtifactGroup.each {diffArtifact ->
            //println JsonOutput.prettyPrint(diffArtifact.toString())
            assertNotEquals(ID.NULL_OBJECT, diffArtifact.getLeft().getIndexEntry().getID())
            assertNotEquals(ID.NULL_OBJECT, diffArtifact.getRight().getIndexEntry().getID())
        }
        assertEquals(8, diffArtifactGroup.size())
    }

    @Test
    void test_zipMaterials() {
        specialFixture()
        List<DiffArtifact> diffArtifactList =
                DiffArtifactGroup.zipMaterials(left, right,
                        new IgnoreMetadataKeys.Builder().ignoreKeys("profile", "URL.host", "URL.port", "URL.protocol").build(),
                        new IdentifyMetadataValues.Builder().putAllNameRegexPairs(["URL.query":"\\w{32}"]).build(),
                        new SortKeys("URL.host")
                        )
        assertNotNull(diffArtifactList)
        diffArtifactList.each {diffArtifact ->
            //println JsonOutput.prettyPrint(diffArtifact.toString())
        }
        assertEquals(8, diffArtifactList.size())

    }
}
