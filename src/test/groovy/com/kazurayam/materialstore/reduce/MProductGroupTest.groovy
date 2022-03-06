package com.kazurayam.materialstore.reduce

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

class MProductGroupTest {

    private static Path outputDir =
            Paths.get(".")
                    .resolve("build/tmp/testOutput")
                    .resolve(MProductGroupTest.class.getName())
    private static Path storeDir = outputDir.resolve("store")
    private static Path issue80Dir =
            Paths.get(".").resolve("src/test/fixture/issue#80")


    private Store store
    private JobName jobName
    private JobTimestamp timestampP
    private JobTimestamp timestampD
    private MaterialList left
    private MaterialList right
    private MProductGroup mProductGroup

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
        mProductGroup = MProductGroup.builder(left, right).build()
    }

    @Test
    void test_add_size_get() {
        mProductGroup.add(MProduct.NULL_OBJECT)
        assertEquals(1, mProductGroup.size())
        assertEquals(MProduct.NULL_OBJECT, mProductGroup.get(0))
    }

    @Test
    void test_countWarnings() {
        MProduct tmp =
                new MProduct.Builder(Material.NULL_OBJECT, Material.NULL_OBJECT,
                        JobTimestamp.now())
                        .setQueryOnMetadata(QueryOnMetadata.NULL_OBJECT)
                        .build()
        tmp.setDiffRatio(45.0d)
        mProductGroup.add(tmp)
        assertEquals(1, mProductGroup.countWarnings(0.00d))
        assertEquals(0, mProductGroup.countWarnings(45.00d))
        assertEquals(0, mProductGroup.countWarnings(45.01d))
    }

    @Test
    void test_getResultTimestamp() {
        JobTimestamp resultTimestamp = mProductGroup.getResultTimestamp()
        //println "resultTimestamp=${resultTimestamp.toString()}"
        assertNotEquals(JobTimestamp.NULL_OBJECT, resultTimestamp)
    }

    @Test
    void test_getDescription() {
        String desc = mProductGroup.getDescription(false)
        println JsonOutput.prettyPrint(desc)
    }

    @Test
    void test_getJobName() {
        specialFixture()
        MProductGroup mProductGroup =
                MProductGroup.builder(left, right).build()
        assertEquals(new JobName("MyAdmin_visual_inspection_twins"),
                mProductGroup.getJobName())
    }

    @Test
    void test_iterator() {
        mProductGroup.add(MProduct.NULL_OBJECT)
        mProductGroup.each { MProduct it ->
            assert it == MProduct.NULL_OBJECT
        }
    }

    @Test
    void test_setter_getter_IdentifyMetadataValues() {
        IdentifyMetadataValues imv =
                new IdentifyMetadataValues.Builder()
                        .putAllNameRegexPairs(["URL.query":"\\w{32}"])
                        .build()
        mProductGroup.setIdentifyMetadataValues(imv)
        IdentifyMetadataValues result = mProductGroup.getIdentifyMetadataValues()
        assertEquals(imv, result)
    }

    @Test
    void test_setter_getter_IgnoreMetadataKeys() {
        mProductGroup.setIgnoreMetadataKeys(IgnoreMetadataKeys.NULL_OBJECT)
        IgnoreMetadataKeys ignoreMetadataKeys = mProductGroup.getIgnoreMetadataKeys()
        assertNotNull(ignoreMetadataKeys)
    }

    @Test
    void test_setter_getter_MaterialListLeft() {
        mProductGroup.setMaterialListLeft(MaterialList.NULL_OBJECT)
        MaterialList left = mProductGroup.getMaterialListLeft()
        assertNotNull(left)
    }

    @Test
    void test_setter_getter_MaterialListRight() {
        mProductGroup.setMaterialListRight(MaterialList.NULL_OBJECT)
        MaterialList right = mProductGroup.getMaterialListRight()
        assertNotNull(right)
    }

    @Test
    void test_toString() {
        mProductGroup.add(MProduct.NULL_OBJECT)
        String s = mProductGroup.toString()
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
                QueryOnMetadata.builder(["profile": "MyAdmin_ProductionEnv" ]).build()
        )
        timestampD = new JobTimestamp("20220128_191342")
        right = store.select(jobName, timestampD,
                QueryOnMetadata.builder(["profile": "MyAdmin_DevelopmentEnv" ]).build()
        )
    }

    @Test
    void test_Builder() {
        specialFixture()
        MProductGroup mProductGroup =
                MProductGroup.builder(left, right)
                        .ignoreKeys("profile", "URL.host", "URL.port", "URL.protocol")
                        .identifyWithRegex(["URL.query":"\\w{32}"])
                        .sort("URL.path")
                        .build()
        assertNotNull(mProductGroup)
        mProductGroup.each {mProduct ->
            //println JsonOutput.prettyPrint(mProduct.toString())
            assertNotEquals(ID.NULL_OBJECT, mProduct.getLeft().getIndexEntry().getID())
            assertNotEquals(ID.NULL_OBJECT, mProduct.getRight().getIndexEntry().getID())
        }
        assertEquals(8, mProductGroup.size())
    }

    @Test
    void test_update() {
        specialFixture()
        MProductGroup mProductGroup =
                new MProductGroup.Builder(left, right)
                        .ignoreKeys("profile", "URL.host", "URL.port", "URL.protocol")
                        .identifyWithRegex(["URL.query":"\\w{32}"])
                        .sort("URL.host")
                        .build()
        int theSize = mProductGroup.size()
        assertEquals(8, theSize)
        //
        MProduct target = mProductGroup.get(0)
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
        MProduct clone = new MProduct(target)
        // let's update it
        mProductGroup.update(clone)
        // now the head is not equal to the clone
        assertNotEquals(mProductGroup.get(0), clone)
        // the tail is equal to the clone
        assertEquals(mProductGroup.get(theSize - 1), clone)
        //
        //println JsonOutput.prettyPrint(mProductGroup.get(theSize - 1).toString())
    }

    @Test
    void test_zipMaterials() {
        specialFixture()
        List<MProduct> mProductList =
                MProductGroup.zipMaterials(
                        left, right, JobTimestamp.now(),
                        new IgnoreMetadataKeys.Builder().ignoreKeys("profile", "URL.host", "URL.port", "URL.protocol").build(),
                        new IdentifyMetadataValues.Builder().putAllNameRegexPairs(["URL.query":"\\w{32}"]).build(),
                        new SortKeys("URL.host")
                        )
        assertNotNull(mProductList)
        mProductList.each {mProduct ->
            //println JsonOutput.prettyPrint(mProduct.toString())
            assertTrue(mProduct.getReducedTimestamp() != JobTimestamp.NULL_OBJECT)
        }
        assertEquals(8, mProductList.size())

    }
}
