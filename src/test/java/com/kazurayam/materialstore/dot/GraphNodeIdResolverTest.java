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
import com.kazurayam.materialstore.reduce.zipper.MaterialProduct;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GraphNodeIdResolverTest {

    private static final Path outputDir =
            Paths.get(System.getProperty("user.dir"))
                    .resolve("build/tmp/testOutput")
                    .resolve(GraphNodeIdResolverTest.class.getName());

    private static final Path issue259Dir =
            Paths.get(".")
                    .resolve("src/test/fixture/issue#259");

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
        FileUtils.copyDirectory(issue259Dir.resolve("store").toFile(), store.getRoot().toFile());
        jobName = new JobName("Main_Twins");
        leftJobTimestamp = new JobTimestamp("20220522_094639");
        rightJobTimestamp = new JobTimestamp("20220522_094706");
    }

    @Test
    public void test_getGraphNodeId_Material_solo() throws MaterialstoreException {
        Material material = store.selectSingle(jobName, leftJobTimestamp, FileType.PNG, QueryOnMetadata.ANY);
        GraphNodeId graphNodeId = GraphNodeIdResolver.getGraphNodeId(material);
        assertEquals(new GraphNodeId("Md5931b2"), graphNodeId);
    }

    @Test
    public void test_getGraphNodeId_Material_in_MaterialList() throws MaterialstoreException {
        MaterialList materialList = store.select(jobName, leftJobTimestamp, FileType.PNG);
        Material material = materialList.get(0);
        GraphNodeId graphNodeId = GraphNodeIdResolver.getGraphNodeId(materialList, material);
        assertEquals(new GraphNodeId("MLa98d469_Md5931b2"), graphNodeId);
    }

    @Test
    public void test_getGraphNodeId_Material_in_MaterialProduct() throws MaterialstoreException {
        MaterialList leftMaterialList = store.select(jobName, leftJobTimestamp, FileType.PNG);
        MaterialList rightMaterialList = store.select(jobName, rightJobTimestamp, FileType.PNG);
        JobName jobName = new JobName("test_getGraphNodeId_Material_in_MaterialProduct");
        JobTimestamp reducedTimestamp = JobTimestamp.now();
        JobTimestamp outJobTimestamp = JobTimestamp.laterThan(reducedTimestamp);
        MaterialProduct mProduct =
                new MaterialProduct.Builder(
                        leftMaterialList.get(0),
                        rightMaterialList.get(0),
                        reducedTimestamp).build();
        GraphNodeId graphNodeId = GraphNodeIdResolver.getGraphNodeId(mProduct, leftMaterialList.get(0));
        assertEquals(new GraphNodeId("MP57d34be_Md5931b2L"), graphNodeId);
    }

    @Test
    public void test_getGraphNodeId_Material_in_MProductGroup() throws MaterialstoreException {
        MaterialList leftMaterialList = store.select(jobName, leftJobTimestamp);
        MaterialList rightMaterialList = store.select(jobName, rightJobTimestamp);
        MProductGroup mProductGroup =
                new MProductGroup.Builder(leftMaterialList, rightMaterialList)
                        .ignoreKeys("profile", "URL.host")
                        .build();
        assert mProductGroup.size() > 0;
        Material material = leftMaterialList.get(0);
        GraphNodeId graphNodeId = GraphNodeIdResolver.getGraphNodeId(mProductGroup, material);
        assertEquals(new GraphNodeId("MPG3db42ca_MP0e22891_M5bd4611L"), graphNodeId);
    }

    @Test
    public void test_getGraphNodeId_Material_in_MProductGroup_before_Zip() throws MaterialstoreException {
        MaterialList leftMaterialList = store.select(jobName, leftJobTimestamp, FileType.PNG);
        MaterialList rightMaterialList = store.select(jobName, rightJobTimestamp, FileType.PNG);
        MProductGroup mProductGroup =
                new MProductGroup.Builder(
                        leftMaterialList,
                        rightMaterialList).ignoreKeys("profile", "URL.host").build();
        Material material = leftMaterialList.get(0);
        GraphNodeId graphNodeId = GraphNodeIdResolver.getGraphNodeIdBeforeZIP(mProductGroup, material);
        assertEquals(new GraphNodeId("MPGBZ2810bee_MLa98d469_Md5931b2L"), graphNodeId);
    }
}
