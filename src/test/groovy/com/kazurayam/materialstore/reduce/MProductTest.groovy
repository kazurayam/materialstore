package com.kazurayam.materialstore.reduce

import com.kazurayam.materialstore.filesystem.FileType
import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.JobTimestamp
import com.kazurayam.materialstore.filesystem.Jobber
import com.kazurayam.materialstore.filesystem.Material
import com.kazurayam.materialstore.filesystem.MaterialList
import com.kazurayam.materialstore.filesystem.Store
import com.kazurayam.materialstore.filesystem.Stores
import com.kazurayam.materialstore.metadata.QueryOnMetadata
import com.kazurayam.materialstore.metadata.SortKeys
import groovy.json.JsonOutput
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.regex.Pattern

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull

class MProductTest {

    private static Path outputDir =
            Paths.get(".").resolve("build/tmp/testOutput")
                    .resolve(MProductTest.class.getName())

    private static Path resultsDir =
            Paths.get(".").resolve("src/test/fixture/sample_results")

    @BeforeAll
    static void beforeAll() {
        if (Files.exists(outputDir)) {
            FileUtils.deleteDirectory(outputDir.toFile())
        }
        Files.createDirectories(outputDir)
    }


    @Test
    void test_getDescription_more() {
        QueryOnMetadata mp = QueryOnMetadata.builder([
                "URL.path": "/",
                "profile": "Flaskr_ProductionEnv",
                "step":"6"
        ]).build()
        SortKeys sortKeys =
                new SortKeys("step", "profile")
        MProduct mProduct =
                new MProduct.Builder(
                        Material.NULL_OBJECT, Material.NULL_OBJECT, JobTimestamp.now())
                        .setQueryOnMetadata(mp)
                        .sortKeys(sortKeys)
                        .build()
        String description = mProduct.getDescription()
        assertEquals('''{"step":"6", "profile":"Flaskr_ProductionEnv", "URL.path":"/"}''',
                description)
    }

    @Test
    void test_getDescription() {
        QueryOnMetadata mp = QueryOnMetadata.builder([
                "URL.host": "demoaut-mimic.kazurayam.com",
                "URL.file": "/"
        ]).build()
        MProduct mProduct =
                new MProduct.Builder(
                        Material.NULL_OBJECT, Material.NULL_OBJECT, JobTimestamp.now())
                        .setQueryOnMetadata(mp)
                        .build()
        assertEquals(
                '''{"URL.file":"/", "URL.host":"demoaut-mimic.kazurayam.com"}''',
                mProduct.getDescription())
    }

    @Test
    void test_toString() {
        QueryOnMetadata mp = QueryOnMetadata.builder([
                "URL.host": "demoaut-mimic.kazurayam.com",
                "URL.file": "/"
        ]).build()
        MProduct mProduct =
                new MProduct.Builder(
                        Material.NULL_OBJECT, Material.NULL_OBJECT, JobTimestamp.now())
                        .setQueryOnMetadata(mp)
                        .build()
        println JsonOutput.prettyPrint(mProduct.toString())
    }

    @Test
    void test_toString_alt() {
        Path root = outputDir.resolve("store")
        Store store = Stores.newInstance(root)
        JobName jobName = new JobName("test_toString_alt")
        // stuff the Job directory with a fixture
        Path jobNameDir = root.resolve(jobName.toString())
        FileUtils.copyDirectory(resultsDir.toFile(), jobNameDir.toFile())
        //
        Jobber jobberOfLeft = store.getJobber(jobName,
                new JobTimestamp("20210715_145922"))
        MaterialList leftList = jobberOfLeft.selectMaterials(
                QueryOnMetadata.builder()
                        .put("profile", "ProductionEnv")
                        .put("URL.file", Pattern.compile(".*"))
                        .build(),
                FileType.PNG)
        assert 2 == leftList.size()
        //
        Jobber jobberOfRight = store.getJobber(jobName,
                new JobTimestamp("20210715_145922"))
        MaterialList rightList= jobberOfRight.selectMaterials(
                QueryOnMetadata.builder()
                        .put("profile", "DevelopmentEnv")
                        .put("URL.file", Pattern.compile(".*"))
                        .build(),
                FileType.PNG)
        assert 2 == rightList.size()
        //
        MProductGroup mProductGroup =
                MProductGroup.builder(leftList, rightList)
                        .ignoreKeys("profile", "URL", "URL.host", "category")
                        .build()
        assertNotNull(mProductGroup)

        println JsonOutput.prettyPrint(mProductGroup.toString())

        assert 2 == mProductGroup.size()
        //
        println mProductGroup.get(0).toString()
    }

}
