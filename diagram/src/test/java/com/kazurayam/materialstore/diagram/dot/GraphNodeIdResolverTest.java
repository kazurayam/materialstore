package com.kazurayam.materialstore.diagram.dot;

import com.kazurayam.materialstore.core.filesystem.FileType;
import com.kazurayam.materialstore.core.filesystem.JobName;
import com.kazurayam.materialstore.core.filesystem.JobTimestamp;
import com.kazurayam.materialstore.core.filesystem.Material;
import com.kazurayam.materialstore.core.filesystem.MaterialList;
import com.kazurayam.materialstore.core.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.core.filesystem.QueryOnMetadata;
import com.kazurayam.materialstore.core.filesystem.Store;
import com.kazurayam.materialstore.core.filesystem.Stores;
import com.kazurayam.materialstore.base.reduce.MaterialProductGroup;
import com.kazurayam.materialstore.base.reduce.zipper.MaterialProduct;
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
                    .resolve("src/test/fixtures/issue#259");

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
        GraphNodeId graphNodeId = GraphNodeIdResolver.resolveIdOfMaterialSolo(material);
        assertEquals(new GraphNodeId("M561b438"), graphNodeId);
    }

    @Test
    public void test_getGraphNodeId_Material_in_MaterialList() throws MaterialstoreException {
        MaterialList materialList = store.select(jobName, leftJobTimestamp, FileType.PNG);
        Material material = materialList.get(0);
        GraphNodeId graphNodeId = GraphNodeIdResolver.resolveIdOfMaterialInMaterialList(materialList, material);
        assertEquals(new GraphNodeId("MLe25845c_M561b438"), graphNodeId);
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
        GraphNodeId graphNodeId = GraphNodeIdResolver.resolveIdOfMaterialInMaterialProduct(mProduct, Role.L);
        assertEquals(new GraphNodeId("MP015220a_M561b438_L"), graphNodeId);
    }

    @Test
    public void test_getGraphNodeId_Material_in_MProductGroup() throws MaterialstoreException {
        MaterialList leftMaterialList = store.select(jobName, leftJobTimestamp);
        MaterialList rightMaterialList = store.select(jobName, rightJobTimestamp);
        MaterialProductGroup mProductGroup =
                new MaterialProductGroup.Builder(leftMaterialList, rightMaterialList)
                        .ignoreKeys("profile", "URL.host")
                        .build();
        assert mProductGroup.size() > 0;
        Material material = leftMaterialList.get(0);
        GraphNodeId graphNodeId = GraphNodeIdResolver.resolveIdOfMaterialInMProductGroup(mProductGroup, material);
        assertEquals(new GraphNodeId("MPG55bdbaa_M99ed902"), graphNodeId);
    }

    @Test
    public void test_getGraphNodeId_Material_in_MProductGroup_before_Zip() throws MaterialstoreException {
        MaterialList leftMaterialList = store.select(jobName, leftJobTimestamp, FileType.PNG);
        MaterialList rightMaterialList = store.select(jobName, rightJobTimestamp, FileType.PNG);
        MaterialProductGroup mProductGroup =
                new MaterialProductGroup.Builder(
                        leftMaterialList,
                        rightMaterialList).ignoreKeys("profile", "URL.host").build();
        Material material = leftMaterialList.get(0);
        GraphNodeId graphNodeId = GraphNodeIdResolver.resolveIdOfMaterialInMProductGroupBeforeZIP(mProductGroup, material);
        assertEquals(new GraphNodeId("MPGBZdaa650e_MLe25845c_M561b438"), graphNodeId);
    }
}
