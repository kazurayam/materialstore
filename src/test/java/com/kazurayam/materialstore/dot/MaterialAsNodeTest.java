package com.kazurayam.materialstore.dot;

import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.FileType;
import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.filesystem.QueryOnMetadata;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.filesystem.Stores;
import com.kazurayam.materialstore.reduce.MaterialProduct;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class MaterialAsNodeTest {

    private static final Path outputDir =
            Paths.get(System.getProperty("user.dir"))
                    .resolve("build/tmp/testOutput")
                    .resolve(MaterialAsNodeTest.class.getName());

    private static final Path issue80Dir =
            Paths.get(".")
                    .resolve("src/test/fixture/issue#80");

    private static Store store;
    private static JobName jobName;
    private static JobTimestamp leftJobTimestamp;
    private static JobTimestamp rightJobTimestamp;


    @BeforeAll
    public static void beforeAll() throws IOException {
        if (Files.exists(outputDir)) {
            FileUtils.deleteDirectory(outputDir.toFile());
        }
        Files.createDirectories(outputDir);
        Path root = outputDir.resolve("store");
        store = Stores.newInstance(root);
        // copy a fixture into the store
        FileUtils.copyDirectory(issue80Dir.toFile(), store.getRoot().toFile());
        jobName = new JobName("MyAdmin_visual_inspection_twins");
        leftJobTimestamp = new JobTimestamp("20220128_191320");
        rightJobTimestamp = new JobTimestamp("20220128_191342");
    }

    @Test
    public void test_MaterialSolo() throws MaterialstoreException {
        Material material = store.selectSingle(jobName, leftJobTimestamp, FileType.PNG, QueryOnMetadata.ANY);
        MaterialSolo solo = new MaterialSolo(material);
        assertEquals("Mc776e59", solo.getNodeId());
    }

    @Test
    public void test_MaterialSolo_derived_from_NULL_OBJECT() throws MaterialstoreException {
        MaterialSolo solo1 = new MaterialSolo(Material.NULL_OBJECT);
        String node1Id1 = solo1.getNodeId();
        assertEquals(8, node1Id1.length());
        String node1Id2 = solo1.getNodeId();
        // an instance of MaterialSolo has its own node id
        assertEquals(node1Id1, node1Id2);
        MaterialSolo solo2 = new MaterialSolo(Material.NULL_OBJECT);
        // 2 instances of MaterialSolo, both are derived from Materials.NULL_OBJECT, have their own unique node id
        assertNotEquals(node1Id1, solo2.getNodeId());
    }


    @Test
    public void test_MaterialInMaterialList() throws MaterialstoreException {
        MaterialList leftList = store.select(jobName, leftJobTimestamp);
        assert leftList.size() > 0;
        Material material = leftList.get(0);
        MaterialInMaterialList miml = new MaterialInMaterialList(leftList, material);
        assertEquals("ML561b663M1865ddd", miml.getNodeId());
    }

    @Test
    public void test_MaterialInMaterialProduct() throws MaterialstoreException {
        Material left = store.selectSingle(jobName, leftJobTimestamp, FileType.PNG);
        Material right = store.selectSingle(jobName, rightJobTimestamp, FileType.PNG);
        JobTimestamp resultJobTimestamp = new JobTimestamp("20220429_173000");
        MaterialProduct mp =
                new MaterialProduct.Builder(left, right, resultJobTimestamp).build();
        MaterialInMaterialProduct mimp = new MaterialInMaterialProduct(mp, left);
        assertEquals("MP30a04c0Mc776e59L", mimp.getNodeId());
    }

    @Test
    public void test_MaterialInMProductGroupBuilder() throws MaterialstoreException {
        Material left = store.selectSingle(jobName, leftJobTimestamp, FileType.PNG);
        Material right = store.selectSingle(jobName, rightJobTimestamp, FileType.PNG);
        JobTimestamp resultJobTimestamp = new JobTimestamp("20220429_173000");
        MaterialProduct.Builder mpBuilder =
                new MaterialProduct.Builder(left, right, resultJobTimestamp);
        //MaterialInMProductGroupBuilder mimpb = new MaterialInMProductGroupBuilder(mpBuilder, left);
        //assertEquals("MP30a04c0Mc776e59L", mimpb.getNodeId());
    }

    @Disabled
    @Test
    public void test_MaterialInMProductGroup() throws MaterialstoreException {
        throw new RuntimeException("TODO");
    }
}
