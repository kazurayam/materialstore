package com.kazurayam.materialstore.base.reduce;

import com.kazurayam.materialstore.core.filesystem.FileType;
import com.kazurayam.materialstore.core.filesystem.ID;
import com.kazurayam.materialstore.core.filesystem.JobName;
import com.kazurayam.materialstore.core.filesystem.JobTimestamp;
import com.kazurayam.materialstore.core.filesystem.Material;
import com.kazurayam.materialstore.core.filesystem.MaterialList;
import com.kazurayam.materialstore.core.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.core.filesystem.Metadata;
import com.kazurayam.materialstore.core.filesystem.QueryOnMetadata;
import com.kazurayam.materialstore.core.filesystem.Store;
import com.kazurayam.materialstore.core.filesystem.Stores;
import com.kazurayam.materialstore.core.filesystem.metadata.IdentifyMetadataValues;
import com.kazurayam.materialstore.core.filesystem.metadata.IgnoreMetadataKeys;
import com.kazurayam.materialstore.core.filesystem.SortKeys;
import com.kazurayam.materialstore.base.reduce.zipper.MaterialProduct;
import com.kazurayam.materialstore.core.util.JsonUtil;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MaterialProductGroupTest {

    private static final Path issue80Dir =
            Paths.get(".")
                    .resolve("src/test/fixtures/issue#80");

    private static final Path outputDir =
            Paths.get(".")
                    .resolve("build/tmp/testOutput")
                    .resolve(MaterialProductGroupTest.class.getName());

    private static Store store;
    private static JobName jobName;
    private static MaterialList left;
    private static MaterialList right;
    private MaterialProductGroup mpg;    // constructed with MaterialList.NULL_OBJECTs

    @BeforeAll
    public static void beforeAll() throws IOException, MaterialstoreException {
        if (Files.exists(outputDir)) {
            FileUtils.deleteDirectory(outputDir.toFile());
        }
        Path root = outputDir.resolve("store");
        store = Stores.newInstance(root);
        Files.createDirectories(outputDir);
        FileUtils.copyDirectory(issue80Dir.toFile(), store.getRoot().toFile());
        //
        jobName = new JobName("MyAdmin_visual_inspection_twins");
        JobTimestamp timestampP = new JobTimestamp("20220128_191320");
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>(1);
        map.put("environment", "MyAdmin_ProductionEnv");
        left = store.select(jobName, timestampP, QueryOnMetadata.builder(map).build());
        assert left.size() > 0;
        JobTimestamp timestampD = new JobTimestamp("20220128_191342");
        LinkedHashMap<String, String> map1 = new LinkedHashMap<String, String>(1);
        map1.put("environment", "MyAdmin_DevelopmentEnv");
        right = store.select(jobName, timestampD, QueryOnMetadata.builder(map1).build());
        assert right.size() > 0;
    }

    @BeforeEach
    public void beforeEach() {
        mpg =
                MaterialProductGroup.builder(
                        MaterialList.NULL_OBJECT, MaterialList.NULL_OBJECT).build();
    }

    @Test
    public void test_add_size_get() {
        mpg.add(MaterialProduct.NULL_OBJECT);
        assertEquals(1, mpg.size());
    }

    @Test
    public void test_countWarnings() {
        MaterialProduct tmp = new MaterialProduct.Builder(
                Material.newEmptyMaterial(),
                Material.newEmptyMaterial(),
                JobTimestamp.now())
                .setQueryOnMetadata(QueryOnMetadata.NULL_OBJECT)
                .build();
        tmp.setDiffRatio(45.0d);
        mpg.add(tmp);
        assertEquals(1, mpg.countWarnings(0.00d));
        assertEquals(0, mpg.countWarnings(45.00d));
        assertEquals(0, mpg.countWarnings(45.01d));
    }

    @Test
    public void test_getCountWarning() {
        MaterialProduct tmp =
                new MaterialProduct
                        .Builder(Material.newEmptyMaterial(), Material.newEmptyMaterial(), JobTimestamp.now())
                        .setQueryOnMetadata(QueryOnMetadata.NULL_OBJECT).build();
        mpg.add(tmp);
        assertEquals(0, mpg.getCountWarning());
    }

    @Test
    public void test_getNumberOfBachelors() {
        MaterialProduct tmp =
                new MaterialProduct.Builder(Material.newEmptyMaterial(), Material.newEmptyMaterial(),
                        JobTimestamp.now())
                        .setQueryOnMetadata(QueryOnMetadata.NULL_OBJECT)
                        .build();
        mpg.add(tmp);
        assertEquals(1, mpg.getNumberOfBachelors());
    }

    @Test
    public void test_getCountTotal() {
        MaterialProduct tmp =
                new MaterialProduct.Builder(Material.newEmptyMaterial(), Material.newEmptyMaterial(), JobTimestamp.now())
                        .setQueryOnMetadata(QueryOnMetadata.NULL_OBJECT)
                        .build();
        mpg.add(tmp);
        assertEquals(1, mpg.getCountTotal());
    }

    @Test
    public void test_getResultTimestamp() {
        JobTimestamp resultTimestamp = mpg.getJobTimestampOfReduceResult();
        //println "resultTimestamp=${resultTimestamp.toString()}"
        Assertions.assertNotEquals(JobTimestamp.NULL_OBJECT, resultTimestamp);
    }

    @Test
    public void test_toVariableJson() {
        String desc = mpg.toVariableJson(new SortKeys(), true);
        System.out.println(JsonUtil.prettyPrint(desc));
    }

    @Test
    public void test_getEnvironmentLeft() {
        MaterialProductGroup mpg =
                MaterialProductGroup.builder(left, right)
                        .environmentLeft("ProductionEnv").build();
        assertEquals(
                "ProductionEnv",
                mpg.getEnvironmentLeft());
    }

    @Test
    public void test_getEnvironmentRight() {
        MaterialProductGroup mpg =
                MaterialProductGroup.builder(left, right)
                        .environmentRight("DevelopmentEnv").build();
        assertEquals(
                "DevelopmentEnv",
                mpg.getEnvironmentRight());
    }

    @Test
    public void test_getJobName() {
        MaterialProductGroup mpg = MaterialProductGroup.builder(left, right).build();
        assertEquals(
                new JobName("MyAdmin_visual_inspection_twins"),
                mpg.getJobName());
    }

    @Test
    public void test_iterator() {
        mpg.add(MaterialProduct.NULL_OBJECT);
        Iterator<MaterialProduct> iter = mpg.iterator();
        while (iter.hasNext()) {
            MaterialProduct mProduct = iter.next();
            assert mProduct.equals(MaterialProduct.NULL_OBJECT);
        }
    }

    @Test
    public void test_NULL_OBJECT() {
        MaterialProductGroup mpg = MaterialProductGroup.NULL_OBJECT;
        assertNotNull(mpg);
        //System.out.println(mpg.toString());
    }

    @Test
    public void test_setter_getter_IdentifyMetadataValues() {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>(1);
        map.put("URL.query", "\\w{32}");
        IdentifyMetadataValues imv = new IdentifyMetadataValues.Builder().putAllNameRegexPairs(map).build();
        mpg.setIdentifyMetadataValues(imv);
        IdentifyMetadataValues result = mpg.getIdentifyMetadataValues();
        assertEquals(imv, result);
    }

    @Test
    public void test_setter_getter_IgnoreMetadataKeys() {
        mpg.setIgnoreMetadataKeys(IgnoreMetadataKeys.NULL_OBJECT);
        IgnoreMetadataKeys ignoreMetadataKeys = mpg.getIgnoreMetadataKeys();
        assertNotNull(ignoreMetadataKeys);
    }

    @Test
    public void test_setter_getter_MaterialListLeft() {
        mpg.setMaterialListLeft(MaterialList.NULL_OBJECT);
        MaterialList left = mpg.getMaterialListLeft();
        assertNotNull(left);
    }

    @Test
    public void test_setter_getter_MaterialListRight() {
        mpg.setMaterialListRight(MaterialList.NULL_OBJECT);
        MaterialList right = mpg.getMaterialListRight();
        assertNotNull(right);
    }


    @Test
    public void test_toString() {
        mpg.add(MaterialProduct.NULL_OBJECT);
        String s = mpg.toString();
        System.out.println(JsonUtil.prettyPrint(s));
        Assertions.assertTrue(s.contains("left"), s);
        Assertions.assertTrue(s.contains("right"), s);
        Assertions.assertTrue(s.contains("diff"), s);
        Assertions.assertTrue(s.contains("queryOnMetadata"), s);
        Assertions.assertTrue(s.contains("diffRatio"), s);
    }

    @Test
    public void test_toSummary() {
        mpg.add(MaterialProduct.NULL_OBJECT);
        String s = mpg.toSummary();
        System.out.println(JsonUtil.prettyPrint(s));
    }


    @Test
    public void test_Builder() throws MaterialstoreException {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>(1);
        map.put("URL.query", "\\w{32}");
        MaterialProductGroup mpg = MaterialProductGroup.builder(left, right).ignoreKeys("environment", "URL.host", "URL.port", "URL.protocol").identifyWithRegex(map).sort("URL.path").build();
        assertNotNull(mpg);
        mpg.forEach( mProduct -> {
            Assertions.assertNotEquals(ID.NULL_OBJECT, ((MaterialProduct) mProduct).getLeft().getIndexEntry().getID());
            Assertions.assertNotEquals(ID.NULL_OBJECT, ((MaterialProduct) mProduct).getRight().getIndexEntry().getID());
        });
        assertEquals(8, mpg.size());
    }

    @Test
    public void test_update() throws MaterialstoreException {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>(1);
        map.put("URL.query", "\\w{32}");
        MaterialProductGroup mpg = new MaterialProductGroup.Builder(left, right).ignoreKeys("environment", "URL.host", "URL.port", "URL.protocol").identifyWithRegex(map).sort("URL.host").build();
        int theSize = mpg.size();
        assertEquals(8, theSize);
        //
        MaterialProduct target = mpg.get(0);
        // make a clone of the target
        MaterialProduct clone = new MaterialProduct(target);
        // let's update it
        mpg.update(clone);
        // now the head is not equal to the clone
        Assertions.assertNotEquals(mpg.get(0), clone);
        // the tail is equal to the clone
        assertEquals(mpg.get(theSize - 1), clone);
        //
        //println JsonOutput.prettyPrint(mpg.get(theSize - 1).toString())
    }


    /**
     * FIXME:
     * this test should be moved to the MProductGroupTest class
     */
    @Test
    public void test_toTemplateModel() throws MaterialstoreException {
        MaterialProductGroup mpg =
                new MaterialProductGroup.Builder(left, right).build();
        JobName jobName = new JobName("test_toTemplateModel");
        // save json for debug
        JobTimestamp jobTimestamp = JobTimestamp.now();
        Metadata metadata = Metadata.builder().build();
        store.write(jobName, jobTimestamp, FileType.JSON,
                metadata, mpg.toJson(true));
        // call toTemplateModel()
        Map<String, Object> model = mpg.toTemplateModel();
        assertNotNull(model);
    }

}
