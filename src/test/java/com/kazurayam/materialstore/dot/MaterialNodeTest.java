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
import com.kazurayam.materialstore.reduce.MProductGroup;
import com.kazurayam.materialstore.reduce.MaterialProduct;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class MaterialNodeTest {

    private static final Path outputDir =
            Paths.get(System.getProperty("user.dir"))
                    .resolve("build/tmp/testOutput")
                    .resolve(MaterialNodeTest.class.getName());

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
        assertEquals(new GraphNodeId("Md5931b2"), solo.getGraphNodeId());
    }

    @Test
    public void test_MaterialSolo_derived_from_NULL_OBJECT() throws MaterialstoreException {
        MaterialSolo solo1 = new MaterialSolo(Material.NULL_OBJECT);
        GraphNodeId node1Id1 = solo1.getGraphNodeId();
        assertEquals(8, node1Id1.getValue().length());
        GraphNodeId node1Id2 = solo1.getGraphNodeId();
        // an instance of MaterialSolo has its own node id
        assertEquals(node1Id1, node1Id2);
        MaterialSolo solo2 = new MaterialSolo(Material.NULL_OBJECT);
        // 2 instances of MaterialSolo, both are derived from Materials.NULL_OBJECT, have their own unique node id
        assertNotEquals(node1Id1, solo2.getGraphNodeId());
    }


    @Test
    public void test_MaterialInMaterialList() throws MaterialstoreException {
        MaterialList leftList = store.select(jobName, leftJobTimestamp);
        assert leftList.size() > 0;
        Material material = leftList.get(0);
        MaterialInMaterialList materialInML = new MaterialInMaterialList(leftList, material);
        assertEquals(new GraphNodeId("ML561b663M5bd4611"), materialInML.getGraphNodeId());
    }

    @Test
    public void test_MaterialInMaterialProduct() throws MaterialstoreException {
        Material left = store.selectSingle(jobName, leftJobTimestamp, FileType.PNG);
        Material right = store.selectSingle(jobName, rightJobTimestamp, FileType.PNG);
        JobTimestamp resultJobTimestamp = new JobTimestamp("20220429_173000");
        MaterialProduct mp =
                new MaterialProduct.Builder(left, right, resultJobTimestamp).build();
        MaterialInMaterialProduct materialInMP = new MaterialInMaterialProduct(mp, left);
        assertEquals(new GraphNodeId("MP24665ccMd5931b2L"), materialInMP.getGraphNodeId());
    }


    @Test
    public void test_MaterialInMProductGroup() throws MaterialstoreException {
        MaterialList leftMaterialList = store.select(jobName, leftJobTimestamp);
        MaterialList rightMaterialList = store.select(jobName, rightJobTimestamp);
        MProductGroup mpg =
                new MProductGroup.Builder(leftMaterialList, rightMaterialList)
                        .ignoreKeys("profile", "URL.host")
                        .build();
        MaterialInMProductGroup materialInMPG =
                new MaterialInMProductGroup(mpg, leftMaterialList.get(0));
        assertEquals(new GraphNodeId("MPG6f6ed8bMP32ab516M5bd4611L"),
                materialInMPG.getGraphNodeId());
    }

    @Test
    public void test_MaterialInMProductGroupBeforeZip() throws MaterialstoreException {
        MaterialList leftMaterialList = store.select(jobName, leftJobTimestamp);
        MaterialList rightMaterialList = store.select(jobName, rightJobTimestamp);
        MProductGroup mpg =
                new MProductGroup.Builder(leftMaterialList, rightMaterialList)
                        .ignoreKeys("profile", "URL.host")
                        .build();
        MaterialInMProductGroupBeforeZip materialInMPG =
                new MaterialInMProductGroupBeforeZip(mpg, leftMaterialList.get(0));
        assertEquals(new GraphNodeId("MPGBZ6f6ed8bML561b663M5bd4611L"),
                materialInMPG.getGraphNodeId());
    }
}
