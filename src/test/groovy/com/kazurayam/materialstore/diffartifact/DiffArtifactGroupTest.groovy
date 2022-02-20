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
                new DiffArtifact.Builder(Material.NULL_OBJECT, Material.NULL_OBJECT,
                        JobTimestamp.now())
                        .setMetadataPattern(MetadataPattern.NULL_OBJECT)
                        .build()
        tmp.setDiffRatio(45.0d)
        diffArtifactGroup.add(tmp)
        assertEquals(1, diffArtifactGroup.countWarnings(0.00d))
        assertEquals(0, diffArtifactGroup.countWarnings(45.00d))
        assertEquals(0, diffArtifactGroup.countWarnings(45.01d))
    }

    @Test
    void test_getDiffTimestamp() {
        JobTimestamp diffTimestamp = diffArtifactGroup.getDiffTimestamp()
        //println "diffTimestamp=${diffTimestamp.toString()}"
        assertNotEquals(JobTimestamp.NULL_OBJECT, diffTimestamp)
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
    void test_update() {
        specialFixture()
        DiffArtifactGroup diffArtifactGroup =
                new DiffArtifactGroup.Builder(left, right)
                        .ignoreKeys("profile", "URL.host", "URL.port", "URL.protocol")
                        .identifyWithRegex(["URL.query":"\\w{32}"])
                        .sort("URL.host")
                        .build()
        int theSize = diffArtifactGroup.size()
        assertEquals(8, theSize)
        //
        DiffArtifact target = diffArtifactGroup.get(0)
        //println JsonOutput.prettyPrint(target.toString())
        /*
{
    "left": {
        "jobName": "MyAdmin_visual_inspection_twins",
        "jobTimestamp": "20220128_191320",
        "ID": "75f6fc61a4a7beced95470f5ae881e533c3a2d8f",
        "fileType": "html",
        "metadata": {
            "URL.host": "myadmin.kazurayam.com",
            "URL.path": "/",
            "URL.port": "80",
            "URL.protocol": "http",
            "profile": "MyAdmin_ProductionEnv"
        }
    },
    "right": {
        "jobName": "MyAdmin_visual_inspection_twins",
        "jobTimestamp": "20220128_191342",
        "ID": "5d7e467a45a85329612d1f0694f9d726bc14226d",
        "fileType": "html",
        "metadata": {
            "URL.host": "devadmin.kazurayam.com",
            "URL.path": "/",
            "URL.port": "80",
            "URL.protocol": "http",
            "profile": "MyAdmin_DevelopmentEnv"
        }
    },
    "diff": {
        "jobName": "_",
        "jobTimestamp": "_",
        "ID": "0000000000000000000000000000000000000000",
        "fileType": "",
        "metadata": {

        }
    },
    "metadataPattern": {
        "URL.path": "/"
    },
    "diffRatio": 0.0
}
         */
        // make a clone of the target
        DiffArtifact clone = new DiffArtifact(target)
        // let's update it
        diffArtifactGroup.update(clone)
        // now the head is not equal to the clone
        assertNotEquals(diffArtifactGroup.get(0), clone)
        // the tail is equal to the clone
        assertEquals(diffArtifactGroup.get(theSize - 1), clone)
        //
        //println JsonOutput.prettyPrint(diffArtifactGroup.get(theSize - 1).toString())
    }

    @Test
    void test_zipMaterials() {
        specialFixture()
        List<DiffArtifact> diffArtifactList =
                DiffArtifactGroup.zipMaterials(
                        left, right, JobTimestamp.now(),
                        new IgnoreMetadataKeys.Builder().ignoreKeys("profile", "URL.host", "URL.port", "URL.protocol").build(),
                        new IdentifyMetadataValues.Builder().putAllNameRegexPairs(["URL.query":"\\w{32}"]).build(),
                        new SortKeys("URL.host")
                        )
        assertNotNull(diffArtifactList)
        diffArtifactList.each {diffArtifact ->
            //println JsonOutput.prettyPrint(diffArtifact.toString())
            assertTrue(diffArtifact.getDiffTimestamp() != JobTimestamp.NULL_OBJECT)
        }
        assertEquals(8, diffArtifactList.size())

    }
}
