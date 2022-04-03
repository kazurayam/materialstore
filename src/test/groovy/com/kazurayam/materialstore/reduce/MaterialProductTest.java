package com.kazurayam.materialstore.reduce;

import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.FileType;
import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.Jobber;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.filesystem.QueryOnMetadata;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.filesystem.Stores;
import com.kazurayam.materialstore.filesystem.metadata.SortKeys;
import com.kazurayam.materialstore.util.JsonUtil;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class MaterialProductTest {

    private static final Path outputDir = Paths.get(".").resolve("build/tmp/testOutput").resolve(MaterialProductTest.class.getName());
    private static final Path resultsDir = Paths.get(".").resolve("src/test/fixture/sample_results");

    @BeforeAll
    public static void beforeAll() throws IOException {
        if (Files.exists(outputDir)) {
            FileUtils.deleteDirectory(outputDir.toFile());
        }

        Files.createDirectories(outputDir);
    }

    @Test
    public void test_getDescription_more() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(3);
        map.put("URL.path", "/");
        map.put("profile", "Flaskr_ProductionEnv");
        map.put("step", "6");
        QueryOnMetadata mp = QueryOnMetadata.builder(map).build();
        SortKeys sortKeys = new SortKeys("step", "profile");
        MaterialProduct mProduct = new MaterialProduct.Builder(Material.NULL_OBJECT, Material.NULL_OBJECT, JobTimestamp.now()).setQueryOnMetadata(mp).sortKeys(sortKeys).build();
        String description = mProduct.getDescription();
        Assertions.assertEquals("{\"step\":\"6\", \"profile\":\"Flaskr_ProductionEnv\", \"URL.path\":\"/\"}", description);
    }

    @Test
    public void test_getDescription() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(2);
        map.put("URL.host", "demoaut-mimic.kazurayam.com");
        map.put("URL.file", "/");
        QueryOnMetadata mp = QueryOnMetadata.builder(map).build();
        MaterialProduct mProduct = new MaterialProduct.Builder(Material.NULL_OBJECT, Material.NULL_OBJECT, JobTimestamp.now()).setQueryOnMetadata(mp).build();
        Assertions.assertEquals(
                "{\"URL.file\":\"/\", \"URL.host\":\"demoaut-mimic.kazurayam.com\"}",
                mProduct.getDescription());
    }

    @Test
    public void test_toString() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(2);
        map.put("URL.host", "demoaut-mimic.kazurayam.com");
        map.put("URL.file", "/");
        QueryOnMetadata mp = QueryOnMetadata.builder(map).build();
        MaterialProduct mProduct = new MaterialProduct.Builder(Material.NULL_OBJECT, Material.NULL_OBJECT, JobTimestamp.now()).setQueryOnMetadata(mp).build();
        System.out.println(JsonUtil.prettyPrint(mProduct.toString()));
    }

    @Test
    public void test_toJson() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(2);
        map.put("URL.host", "demoaut-mimic.kazurayam.com");
        map.put("URL.file", "/");
        QueryOnMetadata mp = QueryOnMetadata.builder(map).build();
        MaterialProduct mProduct = new MaterialProduct.Builder(Material.NULL_OBJECT, Material.NULL_OBJECT, JobTimestamp.now()).setQueryOnMetadata(mp).build();
        String json = mProduct.toJson();
        System.out.println(json);
    }

    /**
     * FIXME:
     * this test should be moved to the MProductGroupTest class
     */
    @Test
    public void test_MProductGroup_toTemplateModel() throws IOException, MaterialstoreException {
        Path root = outputDir.resolve("store");
        Store store = Stores.newInstance(root);
        JobName jobName = new JobName("test_MProductGroup_toTemplateModel");
        // stuff the Job directory with a fixture
        Path jobNameDir = root.resolve(jobName.toString());
        FileUtils.copyDirectory(resultsDir.toFile(), jobNameDir.toFile());
        //
        Jobber jobberOfLeft = store.getJobber(jobName, new JobTimestamp("20210715_145922"));
        MaterialList leftList = jobberOfLeft.selectMaterials(QueryOnMetadata.builder().put("profile", "ProductionEnv").put("URL.path", Pattern.compile(".*")).build(), FileType.PNG);
        assert 2 == leftList.size();
        //
        Jobber jobberOfRight = store.getJobber(jobName, new JobTimestamp("20210715_145922"));
        MaterialList rightList = jobberOfRight.selectMaterials(QueryOnMetadata.builder().put("profile", "DevelopmentEnv").put("URL.path", Pattern.compile(".*")).build(), FileType.PNG);
        assert 2 == rightList.size();
        //
        MProductGroup mProductGroup = MProductGroup.builder(leftList, rightList).ignoreKeys("profile", "URL", "URL.host", "category").build();
        Assertions.assertNotNull(mProductGroup);
        Assertions.assertEquals(2, mProductGroup.size());

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
        Map<String, Object> model = mProductGroup.toTemplateModel();
        Assertions.assertNotNull(model);


    }

}
