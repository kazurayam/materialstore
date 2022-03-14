package com.kazurayam.materialstore.reduce

import com.kazurayam.materialstore.filesystem.FileType
import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.JobTimestamp
import com.kazurayam.materialstore.filesystem.Jobber
import com.kazurayam.materialstore.filesystem.Material
import com.kazurayam.materialstore.filesystem.MaterialList
import com.kazurayam.materialstore.filesystem.Store
import com.kazurayam.materialstore.filesystem.Stores
import com.kazurayam.materialstore.filesystem.QueryOnMetadata
import com.kazurayam.materialstore.filesystem.metadata.SortKeys
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
    void test_toJson() {
        QueryOnMetadata mp = QueryOnMetadata.builder([
                "URL.host": "demoaut-mimic.kazurayam.com",
                "URL.file": "/"
        ]).build()
        MProduct mProduct =
                new MProduct.Builder(
                        Material.NULL_OBJECT, Material.NULL_OBJECT, JobTimestamp.now())
                        .setQueryOnMetadata(mp)
                        .build()
        String json = mProduct.toJson()
        println json
    }


    /**
     * FIXME:
     * this test should be moved to the MProductGroupTest class
     */
    @Test
    void test_MProductGroup_toTemplateModel() {
        Path root = outputDir.resolve("store")
        Store store = Stores.newInstance(root)
        JobName jobName = new JobName("test_MProductGroup_toTemplateModel")
        // stuff the Job directory with a fixture
        Path jobNameDir = root.resolve(jobName.toString())
        FileUtils.copyDirectory(resultsDir.toFile(), jobNameDir.toFile())
        //
        Jobber jobberOfLeft = store.getJobber(jobName,
                new JobTimestamp("20210715_145922"))
        MaterialList leftList = jobberOfLeft.selectMaterials(
                QueryOnMetadata.builder()
                        .put("profile", "ProductionEnv")
                        .put("URL.path", Pattern.compile(".*"))
                        .build(),
                FileType.PNG)
        assert 2 == leftList.size()
        //
        Jobber jobberOfRight = store.getJobber(jobName,
                new JobTimestamp("20210715_145922"))
        MaterialList rightList= jobberOfRight.selectMaterials(
                QueryOnMetadata.builder()
                        .put("profile", "DevelopmentEnv")
                        .put("URL.path", Pattern.compile(".*"))
                        .build(),
                FileType.PNG)
        assert 2 == rightList.size()
        //
        MProductGroup mProductGroup =
                MProductGroup.builder(leftList, rightList)
                        .ignoreKeys("profile", "URL", "URL.host", "category")
                        .build()
        assertNotNull(mProductGroup)

        //println mProductGroup.toJson()
        /*
{
  "jobName": "test_MProductGroup_toTemplateModel",
  "resultTimestamp": "20220314_101804",
  "isReadyToReport": false,
  "materialList0": {
    "jobTimestamp": "20210715_145922",
    "size": 2.0
  },
  "materialList1": {
    "jobTimestamp": "20210715_145922",
    "size": 2.0
  },
  "mProductList": [
    {
      "reducedTimestamp": "20220314_101804",
      "diffRatio": 0.0,
      "left": {
        "jobName": "test_MProductGroup_toTemplateModel",
        "jobTimestamp": "20210715_145922",
        "id": "71c074d8dc52d9d589b2cbdb08a586717d457d18",
        "fileType": "png",
        "metadata": {
          "URL.host": "demoaut.katalon.com",
          "URL.path": "/",
          "URL.protocol": "http",
          "category": "screenshot",
          "profile": "ProductionEnv",
          "xpath": "//a[@id\u003d\u0027btn-make-appointment\u0027]"
        }
      },
      "right": {
        "jobName": "test_MProductGroup_toTemplateModel",
        "jobTimestamp": "20210715_145922",
        "id": "ba259dadd0142acdc598532c76aa82e1f5a852b0",
        "fileType": "png",
        "metadata": {
          "URL.host": "demoaut-mimic.kazurayam.com",
          "URL.path": "/",
          "URL.protocol": "http",
          "category": "screenshot",
          "profile": "DevelopmentEnv",
          "xpath": "//a[@id\u003d\u0027btn-make-appointment\u0027]"
        }
      },
      "queryOnMetadata": {
        "URL.path": "/",
        "URL.protocol": "http",
        "xpath": "//a[@id\u003d\u0027btn-make-appointment\u0027]"
      },
      "diff": {
        "jobName": "_",
        "jobTimestamp": "_",
        "id": "0000000000000000000000000000000000000000",
        "fileType": "",
        "metadata": {}
      }
    },
    {
      "reducedTimestamp": "20220314_101804",
      "diffRatio": 0.0,
      "left": {
        "jobName": "test_MProductGroup_toTemplateModel",
        "jobTimestamp": "20210715_145922",
        "id": "464212dbd99fbddc5f7442089523fab4b9247b9b",
        "fileType": "png",
        "metadata": {
          "URL.host": "demoaut.katalon.com",
          "URL.path": "/",
          "URL.protocol": "http",
          "category": "screenshot",
          "profile": "ProductionEnv",
          "xpath": "/html"
        }
      },
      "right": {
        "jobName": "test_MProductGroup_toTemplateModel",
        "jobTimestamp": "20210715_145922",
        "id": "75693d3480688939fda48692a31da28440252fe0",
        "fileType": "png",
        "metadata": {
          "URL.host": "demoaut-mimic.kazurayam.com",
          "URL.path": "/",
          "URL.protocol": "http",
          "category": "screenshot",
          "profile": "DevelopmentEnv",
          "xpath": "/html"
        }
      },
      "queryOnMetadata": {
        "URL.path": "/",
        "URL.protocol": "http",
        "xpath": "/html"
      },
      "diff": {
        "jobName": "_",
        "jobTimestamp": "_",
        "id": "0000000000000000000000000000000000000000",
        "fileType": "",
        "metadata": {}
      }
    }
  ]
}
         */
        Map<String, Object> model = mProductGroup.toTemplateModel()
        assertNotNull(model)
        List<Map<String, Object>> mProductList =
                (List<Map<String, Object>>)model.get("mProductList")
        assertEquals(2, mProductList.size())
    }
}
