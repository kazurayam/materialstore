package com.kazurayam.materialstore.resolvent

import com.kazurayam.materialstore.filesystem.ID
import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.JobTimestamp
import com.kazurayam.materialstore.filesystem.Material
import com.kazurayam.materialstore.filesystem.MaterialList
import com.kazurayam.materialstore.filesystem.Store
import com.kazurayam.materialstore.filesystem.Stores
import com.kazurayam.materialstore.metadata.IdentifyMetadataValues
import com.kazurayam.materialstore.metadata.IgnoreMetadataKeys
import com.kazurayam.materialstore.metadata.QueryOnMetadata
import com.kazurayam.materialstore.metadata.SortKeys
import groovy.json.JsonOutput
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.jupiter.api.Assertions.*

class ArtifactGroupTest {

    private static Path outputDir =
            Paths.get(".")
                    .resolve("build/tmp/testOutput")
                    .resolve(ArtifactGroupTest.class.getName())
    private static Path storeDir = outputDir.resolve("store")
    private static Path issue80Dir =
            Paths.get(".").resolve("src/test/fixture/issue#80")


    private Store store
    private JobName jobName
    private JobTimestamp timestampP
    private JobTimestamp timestampD
    private MaterialList left
    private MaterialList right
    private ArtifactGroup artifactGroup

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
        artifactGroup = ArtifactGroup.builder(left, right).build()
    }

    @Test
    void test_add_size_get() {
        artifactGroup.add(Artifact.NULL_OBJECT)
        assertEquals(1, artifactGroup.size())
        assertEquals(Artifact.NULL_OBJECT, artifactGroup.get(0))
    }

    @Test
    void test_countWarnings() {
        Artifact tmp =
                new Artifact.Builder(Material.NULL_OBJECT, Material.NULL_OBJECT,
                        JobTimestamp.now())
                        .setQueryOnMetadata(QueryOnMetadata.NULL_OBJECT)
                        .build()
        tmp.setDiffRatio(45.0d)
        artifactGroup.add(tmp)
        assertEquals(1, artifactGroup.countWarnings(0.00d))
        assertEquals(0, artifactGroup.countWarnings(45.00d))
        assertEquals(0, artifactGroup.countWarnings(45.01d))
    }

    @Test
    void test_getResolventTimestamp() {
        JobTimestamp resolventTimestamp = artifactGroup.getResolventTimestamp()
        //println "resolventTimestamp=${resolventTimestamp.toString()}"
        assertNotEquals(JobTimestamp.NULL_OBJECT, resolventTimestamp)
    }

    @Test
    void test_iterator() {
        artifactGroup.add(Artifact.NULL_OBJECT)
        artifactGroup.each { Artifact it ->
            assert it == Artifact.NULL_OBJECT
        }
    }

    @Test
    void test_setter_getter_IdentifyMetadataValues() {
        IdentifyMetadataValues imv =
                new IdentifyMetadataValues.Builder()
                        .putAllNameRegexPairs(["URL.query":"\\w{32}"])
                        .build()
        artifactGroup.setIdentifyMetadataValues(imv)
        IdentifyMetadataValues result = artifactGroup.getIdentifyMetadataValues()
        assertEquals(imv, result)
    }

    @Test
    void test_setter_getter_IgnoreMetadataKeys() {
        artifactGroup.setIgnoreMetadataKeys(IgnoreMetadataKeys.NULL_OBJECT)
        IgnoreMetadataKeys ignoreMetadataKeys = artifactGroup.getIgnoreMetadataKeys()
        assertNotNull(ignoreMetadataKeys)
    }

    @Test
    void test_setter_getter_LeftMaterialList() {
        artifactGroup.setLeftMaterialList(MaterialList.NULL_OBJECT)
        MaterialList left = artifactGroup.getLeftMaterialList()
        assertNotNull(left)
    }

    @Test
    void test_setter_getter_RightMaterialList() {
        artifactGroup.setRightMaterialList(MaterialList.NULL_OBJECT)
        MaterialList right = artifactGroup.getRightMaterialList()
        assertNotNull(right)
    }

    @Test
    void test_toString() {
        artifactGroup.add(Artifact.NULL_OBJECT)
        String s = artifactGroup.toString()
        println JsonOutput.prettyPrint(s)
        assertTrue(s.contains("left"), s)
        assertTrue(s.contains("right"), s)
        assertTrue(s.contains("diff"), s)
        assertTrue(s.contains("queryOnMetadata"), s)
        assertTrue(s.contains("diffRatio"), s)
    }

    void specialFixture() {
        store = Stores.newInstance(storeDir)
        jobName = new JobName("MyAdmin_visual_inspection_twins")
        timestampP = new JobTimestamp("20220128_191320")
        left = store.select(jobName, timestampP,
                QueryOnMetadata.builderWithMap(["profile": "MyAdmin_ProductionEnv" ]).build()
        )
        timestampD = new JobTimestamp("20220128_191342")
        right = store.select(jobName, timestampD,
                QueryOnMetadata.builderWithMap(["profile": "MyAdmin_DevelopmentEnv" ]).build()
        )
    }

    @Test
    void test_Builder() {
        specialFixture()
        ArtifactGroup artifactGroup =
                ArtifactGroup.builder(left, right)
                        .ignoreKeys("profile", "URL.host", "URL.port", "URL.protocol")
                        .identifyWithRegex(["URL.query":"\\w{32}"])
                        .sort("URL.path")
                        .build()
        assertNotNull(artifactGroup)
        artifactGroup.each {artifact ->
            //println JsonOutput.prettyPrint(artifact.toString())
            assertNotEquals(ID.NULL_OBJECT, artifact.getLeft().getIndexEntry().getID())
            assertNotEquals(ID.NULL_OBJECT, artifact.getRight().getIndexEntry().getID())
        }
        assertEquals(8, artifactGroup.size())
    }

    @Test
    void test_update() {
        specialFixture()
        ArtifactGroup artifactGroup =
                new ArtifactGroup.Builder(left, right)
                        .ignoreKeys("profile", "URL.host", "URL.port", "URL.protocol")
                        .identifyWithRegex(["URL.query":"\\w{32}"])
                        .sort("URL.host")
                        .build()
        int theSize = artifactGroup.size()
        assertEquals(8, theSize)
        //
        Artifact target = artifactGroup.get(0)
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
    "queryOnMetadata": {
        "URL.path": "/"
    },
    "diffRatio": 0.0
}
         */
        // make a clone of the target
        Artifact clone = new Artifact(target)
        // let's update it
        artifactGroup.update(clone)
        // now the head is not equal to the clone
        assertNotEquals(artifactGroup.get(0), clone)
        // the tail is equal to the clone
        assertEquals(artifactGroup.get(theSize - 1), clone)
        //
        //println JsonOutput.prettyPrint(artifactGroup.get(theSize - 1).toString())
    }

    @Test
    void test_zipMaterials() {
        specialFixture()
        List<Artifact> artifactList =
                ArtifactGroup.zipMaterials(
                        left, right, JobTimestamp.now(),
                        new IgnoreMetadataKeys.Builder().ignoreKeys("profile", "URL.host", "URL.port", "URL.protocol").build(),
                        new IdentifyMetadataValues.Builder().putAllNameRegexPairs(["URL.query":"\\w{32}"]).build(),
                        new SortKeys("URL.host")
                        )
        assertNotNull(artifactList)
        artifactList.each {artifact ->
            //println JsonOutput.prettyPrint(artifact.toString())
            assertTrue(artifact.getResolventTimestamp() != JobTimestamp.NULL_OBJECT)
        }
        assertEquals(8, artifactList.size())

    }
}
