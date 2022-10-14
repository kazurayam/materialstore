package com.kazurayam.materialstore.reduce;

import com.kazurayam.materialstore.filesystem.FileType;
import com.kazurayam.materialstore.filesystem.ID;
import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.Metadata;
import com.kazurayam.materialstore.filesystem.QueryOnMetadata;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.filesystem.Stores;
import com.kazurayam.materialstore.filesystem.metadata.IdentifyMetadataValues;
import com.kazurayam.materialstore.filesystem.metadata.IgnoreMetadataKeys;
import com.kazurayam.materialstore.filesystem.metadata.SortKeys;
import com.kazurayam.materialstore.reduce.zipper.MaterialProduct;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MProductGroupTest {

    private static final Path issue80Dir =
            Paths.get(".")
                    .resolve("src/test/fixture/issue#80");

    private static final Path outputDir =
            Paths.get(".")
                    .resolve("build/tmp/testOutput")
                    .resolve(MProductGroupTest.class.getName());

    private static Store store;
    private static JobName jobName;
    private static MaterialList left;
    private static MaterialList right;
    private MProductGroup baseMProductGroup;    // constructed with MaterialList.NULL_OBJECTs

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
        map.put("profile", "MyAdmin_ProductionEnv");
        left = store.select(jobName, timestampP, QueryOnMetadata.builder(map).build());
        assert left.size() > 0;
        JobTimestamp timestampD = new JobTimestamp("20220128_191342");
        LinkedHashMap<String, String> map1 = new LinkedHashMap<String, String>(1);
        map1.put("profile", "MyAdmin_DevelopmentEnv");
        right = store.select(jobName, timestampD, QueryOnMetadata.builder(map1).build());
        assert right.size() > 0;
    }

    @BeforeEach
    public void beforeEach() {
        baseMProductGroup =
                MProductGroup.builder(
                        MaterialList.NULL_OBJECT, MaterialList.NULL_OBJECT).build();
    }

    @Test
    public void test_add_size_get() {
        baseMProductGroup.add(MaterialProduct.NULL_OBJECT);
        assertEquals(1, baseMProductGroup.size());
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
        baseMProductGroup.add(tmp);
        assertEquals(1, baseMProductGroup.countWarnings(0.00d));
        assertEquals(0, baseMProductGroup.countWarnings(45.00d));
        assertEquals(0, baseMProductGroup.countWarnings(45.01d));
    }

    @Test
    public void test_getCountWarning() {
        MaterialProduct tmp =
                new MaterialProduct
                        .Builder(Material.newEmptyMaterial(), Material.newEmptyMaterial(), JobTimestamp.now())
                        .setQueryOnMetadata(QueryOnMetadata.NULL_OBJECT).build();
        baseMProductGroup.add(tmp);
        assertEquals(0, baseMProductGroup.getCountWarning());
    }

    @Test
    public void test_getNumberOfBachelors() {
        MaterialProduct tmp =
                new MaterialProduct.Builder(Material.newEmptyMaterial(), Material.newEmptyMaterial(),
                        JobTimestamp.now())
                        .setQueryOnMetadata(QueryOnMetadata.NULL_OBJECT)
                        .build();
        baseMProductGroup.add(tmp);
        assertEquals(1, baseMProductGroup.getNumberOfBachelors());
    }

    @Test
    public void test_getCountTotal() {
        MaterialProduct tmp =
                new MaterialProduct.Builder(Material.newEmptyMaterial(), Material.newEmptyMaterial(), JobTimestamp.now())
                        .setQueryOnMetadata(QueryOnMetadata.NULL_OBJECT)
                        .build();
        baseMProductGroup.add(tmp);
        assertEquals(1, baseMProductGroup.getCountTotal());
    }

    @Test
    public void test_getResultTimestamp() {
        JobTimestamp resultTimestamp = baseMProductGroup.getResultTimestamp();
        //println "resultTimestamp=${resultTimestamp.toString()}"
        Assertions.assertNotEquals(JobTimestamp.NULL_OBJECT, resultTimestamp);
    }

    @Test
    public void test_toVariableJson() {
        String desc = baseMProductGroup.toVariableJson(new SortKeys(), true);
        System.out.println(JsonUtil.prettyPrint(desc));
    }

    @Test
    public void test_getJobName() {
        MProductGroup mProductGroup = MProductGroup.builder(left, right).build();
        assertEquals(
                new JobName("MyAdmin_visual_inspection_twins"),
                mProductGroup.getJobName());
    }

    @Test
    public void test_iterator() {
        baseMProductGroup.add(MaterialProduct.NULL_OBJECT);
        Iterator<MaterialProduct> iter = baseMProductGroup.iterator();
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
        baseMProductGroup.setIdentifyMetadataValues(imv);
        IdentifyMetadataValues result = baseMProductGroup.getIdentifyMetadataValues();
        assertEquals(imv, result);
    }

    @Test
    public void test_setter_getter_IgnoreMetadataKeys() {
        baseMProductGroup.setIgnoreMetadataKeys(IgnoreMetadataKeys.NULL_OBJECT);
        IgnoreMetadataKeys ignoreMetadataKeys = baseMProductGroup.getIgnoreMetadataKeys();
        Assertions.assertNotNull(ignoreMetadataKeys);
    }

    @Test
    public void test_setter_getter_MaterialListLeft() {
        baseMProductGroup.setMaterialListLeft(MaterialList.NULL_OBJECT);
        MaterialList left = baseMProductGroup.getMaterialListLeft();
        Assertions.assertNotNull(left);
    }

    @Test
    public void test_setter_getter_MaterialListRight() {
        baseMProductGroup.setMaterialListRight(MaterialList.NULL_OBJECT);
        MaterialList right = baseMProductGroup.getMaterialListRight();
        Assertions.assertNotNull(right);
    }


    @Test
    public void test_toString() {
        baseMProductGroup.add(MaterialProduct.NULL_OBJECT);
        String s = baseMProductGroup.toString();
        System.out.println(JsonUtil.prettyPrint(s));
        Assertions.assertTrue(s.contains("left"), s);
        Assertions.assertTrue(s.contains("right"), s);
        Assertions.assertTrue(s.contains("diff"), s);
        Assertions.assertTrue(s.contains("queryOnMetadata"), s);
        Assertions.assertTrue(s.contains("diffRatio"), s);
    }

    @Test
    public void test_toSummary() {
        baseMProductGroup.add(MaterialProduct.NULL_OBJECT);
        String s = baseMProductGroup.toSummary();
        System.out.println(JsonUtil.prettyPrint(s));
    }


    @Test
    public void test_Builder() throws MaterialstoreException {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>(1);
        map.put("URL.query", "\\w{32}");
        MProductGroup mProductGroup = MProductGroup.builder(left, right).ignoreKeys("profile", "URL.host", "URL.port", "URL.protocol").identifyWithRegex(map).sort("URL.path").build();
        Assertions.assertNotNull(mProductGroup);
        mProductGroup.forEach( mProduct -> {
            Assertions.assertNotEquals(ID.NULL_OBJECT, ((MaterialProduct) mProduct).getLeft().getIndexEntry().getID());
            Assertions.assertNotEquals(ID.NULL_OBJECT, ((MaterialProduct) mProduct).getRight().getIndexEntry().getID());
        });
        assertEquals(8, mProductGroup.size());
    }

    @Test
    public void test_update() throws MaterialstoreException {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>(1);
        map.put("URL.query", "\\w{32}");
        MProductGroup mProductGroup = new MProductGroup.Builder(left, right).ignoreKeys("profile", "URL.host", "URL.port", "URL.protocol").identifyWithRegex(map).sort("URL.host").build();
        int theSize = mProductGroup.size();
        assertEquals(8, theSize);
        //
        MaterialProduct target = mProductGroup.get(0);
        // make a clone of the target
        MaterialProduct clone = new MaterialProduct(target);
        // let's update it
        mProductGroup.update(clone);
        // now the head is not equal to the clone
        Assertions.assertNotEquals(mProductGroup.get(0), clone);
        // the tail is equal to the clone
        assertEquals(mProductGroup.get(theSize - 1), clone);
        //
        //println JsonOutput.prettyPrint(mProductGroup.get(theSize - 1).toString())
    }


    /**
     * FIXME:
     * this test should be moved to the MProductGroupTest class
     */
    @Test
    public void test_toTemplateModel() throws MaterialstoreException {
        MProductGroup mProductGroup =
                new MProductGroup.Builder(left, right).build();
        JobName jobName = new JobName("test_toTemplateModel");
        // save json for debug
        JobTimestamp jobTimestamp = JobTimestamp.now();
        Metadata metadata = Metadata.builder().build();
        store.write(jobName, jobTimestamp, FileType.JSON,
                metadata, mProductGroup.toJson(true));
        // call toTemplateModel()
        Map<String, Object> model = mProductGroup.toTemplateModel();
        Assertions.assertNotNull(model);
    }

}
