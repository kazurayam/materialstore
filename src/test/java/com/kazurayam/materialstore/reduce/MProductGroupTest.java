package com.kazurayam.materialstore.reduce;

import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.ID;
import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.filesystem.QueryOnMetadata;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.filesystem.Stores;
import com.kazurayam.materialstore.filesystem.metadata.IdentifyMetadataValues;
import com.kazurayam.materialstore.filesystem.metadata.IgnoreMetadataKeys;
import com.kazurayam.materialstore.filesystem.metadata.SortKeys;
import com.kazurayam.materialstore.util.JsonUtil;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

public class MProductGroupTest {

    private static final Path outputDir = Paths.get(".").resolve("build/tmp/testOutput").resolve(MProductGroupTest.class.getName());
    private static final Path storeDir = outputDir.resolve("store");
    private static final Path issue80Dir = Paths.get(".").resolve("src/test/fixture/issue#80");

    private MaterialList left;
    private MaterialList right;
    private MProductGroup mProductGroup;

    @BeforeAll
    public static void beforeAll() throws IOException {
        if (Files.exists(outputDir)) {
            FileUtils.deleteDirectory(outputDir.toFile());
        }

        Files.createDirectories(outputDir);
        FileUtils.copyDirectory(issue80Dir.toFile(), storeDir.toFile());
    }

    @BeforeEach
    public void before() {
        MaterialList left = MaterialList.NULL_OBJECT;
        MaterialList right = MaterialList.NULL_OBJECT;
        mProductGroup = MProductGroup.builder(left, right).build();
    }

    public void specialFixture() throws MaterialstoreException {
        Store store = Stores.newInstance(storeDir);
        JobName jobName = new JobName("MyAdmin_visual_inspection_twins");
        JobTimestamp timestampP = new JobTimestamp("20220128_191320");
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>(1);
        map.put("profile", "MyAdmin_ProductionEnv");
        left = store.select(jobName, timestampP, QueryOnMetadata.builder(map).build());
        JobTimestamp timestampD = new JobTimestamp("20220128_191342");
        LinkedHashMap<String, String> map1 = new LinkedHashMap<String, String>(1);
        map1.put("profile", "MyAdmin_DevelopmentEnv");
        right = store.select(jobName, timestampD, QueryOnMetadata.builder(map1).build());
    }


    @Test
    public void test_add_size_get() {
        mProductGroup.add(MaterialProduct.NULL_OBJECT);
        Assertions.assertEquals(1, mProductGroup.size());
        Assertions.assertEquals(MaterialProduct.NULL_OBJECT, mProductGroup.get(0));
    }

    @Test
    public void test_countWarnings() {
        MaterialProduct tmp = new MaterialProduct.Builder(Material.NULL_OBJECT, Material.NULL_OBJECT, JobTimestamp.now()).setQueryOnMetadata(QueryOnMetadata.NULL_OBJECT).build();
        tmp.setDiffRatio(45.0d);
        mProductGroup.add(tmp);
        Assertions.assertEquals(1, mProductGroup.countWarnings(0.00d));
        Assertions.assertEquals(0, mProductGroup.countWarnings(45.00d));
        Assertions.assertEquals(0, mProductGroup.countWarnings(45.01d));
    }

    @Test
    public void test_getCountWarning() {
        MaterialProduct tmp = new MaterialProduct.Builder(Material.NULL_OBJECT, Material.NULL_OBJECT, JobTimestamp.now()).setQueryOnMetadata(QueryOnMetadata.NULL_OBJECT).build();
        mProductGroup.add(tmp);
        Assertions.assertEquals(0, mProductGroup.getCountWarning());
    }

    @Test
    public void test_getCountIgnorable() {
        MaterialProduct tmp = new MaterialProduct.Builder(Material.NULL_OBJECT, Material.NULL_OBJECT, JobTimestamp.now()).setQueryOnMetadata(QueryOnMetadata.NULL_OBJECT).build();
        mProductGroup.add(tmp);
        Assertions.assertEquals(0, mProductGroup.getCountIgnorable());
    }

    @Test
    public void test_getCountTotal() {
        MaterialProduct tmp = new MaterialProduct.Builder(Material.NULL_OBJECT, Material.NULL_OBJECT, JobTimestamp.now()).setQueryOnMetadata(QueryOnMetadata.NULL_OBJECT).build();
        mProductGroup.add(tmp);
        Assertions.assertEquals(1, mProductGroup.getCountTotal());
    }

    @Test
    public void test_getResultTimestamp() {
        JobTimestamp resultTimestamp = mProductGroup.getResultTimestamp();
        //println "resultTimestamp=${resultTimestamp.toString()}"
        Assertions.assertNotEquals(JobTimestamp.NULL_OBJECT, resultTimestamp);
    }

    @Test
    public void test_getDescription() {
        String desc = mProductGroup.getDescription(false);
        System.out.println(JsonUtil.prettyPrint(desc));
    }

    @Test
    public void test_getJobName() throws MaterialstoreException {
        specialFixture();
        MProductGroup mProductGroup = MProductGroup.builder(left, right).build();
        Assertions.assertEquals(new JobName("MyAdmin_visual_inspection_twins"), mProductGroup.getJobName());
    }

    @Test
    public void test_iterator() {
        mProductGroup.add(MaterialProduct.NULL_OBJECT);
        Iterator<MaterialProduct> iter = mProductGroup.iterator();
        while (iter.hasNext()) {
            MaterialProduct mProduct = iter.next();
            assert mProduct.equals(MaterialProduct.NULL_OBJECT);
        }
    }

    @Test
    public void test_setter_getter_IdentifyMetadataValues() {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>(1);
        map.put("URL.query", "\\w{32}");
        IdentifyMetadataValues imv = new IdentifyMetadataValues.Builder().putAllNameRegexPairs(map).build();
        mProductGroup.setIdentifyMetadataValues(imv);
        IdentifyMetadataValues result = mProductGroup.getIdentifyMetadataValues();
        Assertions.assertEquals(imv, result);
    }

    @Test
    public void test_setter_getter_IgnoreMetadataKeys() {
        mProductGroup.setIgnoreMetadataKeys(IgnoreMetadataKeys.NULL_OBJECT);
        IgnoreMetadataKeys ignoreMetadataKeys = mProductGroup.getIgnoreMetadataKeys();
        Assertions.assertNotNull(ignoreMetadataKeys);
    }

    @Test
    public void test_setter_getter_MaterialListLeft() {
        mProductGroup.setMaterialListLeft(MaterialList.NULL_OBJECT);
        MaterialList left = mProductGroup.getMaterialListLeft();
        Assertions.assertNotNull(left);
    }

    @Test
    public void test_setter_getter_MaterialListRight() {
        mProductGroup.setMaterialListRight(MaterialList.NULL_OBJECT);
        MaterialList right = mProductGroup.getMaterialListRight();
        Assertions.assertNotNull(right);
    }

    @Test
    public void test_toString() {
        mProductGroup.add(MaterialProduct.NULL_OBJECT);
        String s = mProductGroup.toString();
        System.out.println(JsonUtil.prettyPrint(s));
        Assertions.assertTrue(s.contains("left"), s);
        Assertions.assertTrue(s.contains("right"), s);
        Assertions.assertTrue(s.contains("diff"), s);
        Assertions.assertTrue(s.contains("queryOnMetadata"), s);
        Assertions.assertTrue(s.contains("diffRatio"), s);
    }


    @Test
    public void test_Builder() throws MaterialstoreException {
        specialFixture();
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>(1);
        map.put("URL.query", "\\w{32}");
        MProductGroup mProductGroup = MProductGroup.builder(left, right).ignoreKeys("profile", "URL.host", "URL.port", "URL.protocol").identifyWithRegex(map).sort("URL.path").build();
        Assertions.assertNotNull(mProductGroup);
        mProductGroup.forEach( mProduct -> {
            Assertions.assertNotEquals(ID.NULL_OBJECT, ((MaterialProduct) mProduct).getLeft().getIndexEntry().getID());
            Assertions.assertNotEquals(ID.NULL_OBJECT, ((MaterialProduct) mProduct).getRight().getIndexEntry().getID());
        });
        Assertions.assertEquals(8, mProductGroup.size());
    }

    @Test
    public void test_update() throws MaterialstoreException {
        specialFixture();
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>(1);
        map.put("URL.query", "\\w{32}");
        MProductGroup mProductGroup = new MProductGroup.Builder(left, right).ignoreKeys("profile", "URL.host", "URL.port", "URL.protocol").identifyWithRegex(map).sort("URL.host").build();
        int theSize = mProductGroup.size();
        Assertions.assertEquals(8, theSize);
        //
        MaterialProduct target = mProductGroup.get(0);
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
        MaterialProduct clone = new MaterialProduct(target);
        // let's update it
        mProductGroup.update(clone);
        // now the head is not equal to the clone
        Assertions.assertNotEquals(mProductGroup.get(0), clone);
        // the tail is equal to the clone
        Assertions.assertEquals(mProductGroup.get(theSize - 1), clone);
        //
        //println JsonOutput.prettyPrint(mProductGroup.get(theSize - 1).toString())
    }

    @Test
    public void test_zipMaterials() throws MaterialstoreException {
        specialFixture();
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>(1);
        map.put("URL.query", "\\w{32}");
        List<MaterialProduct> mProductList = MProductGroup.zipMaterials(left, right, JobTimestamp.now(), new IgnoreMetadataKeys.Builder().ignoreKeys("profile", "URL.host", "URL.port", "URL.protocol").build(), new IdentifyMetadataValues.Builder().putAllNameRegexPairs(map).build(), new SortKeys("URL.host"));
        Assertions.assertNotNull(mProductList);
        for (MaterialProduct mProduct : mProductList) {
            //println JsonOutput.prettyPrint(mProduct.toString())
            Assertions.assertFalse(mProduct.getReducedTimestamp().equals(JobTimestamp.NULL_OBJECT));
        }
        Assertions.assertEquals(8, mProductList.size());
    }
}
