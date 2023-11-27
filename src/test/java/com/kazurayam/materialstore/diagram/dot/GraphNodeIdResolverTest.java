package com.kazurayam.materialstore.diagram.dot;

import com.kazurayam.materialstore.TestOutputOrganizerFactory;
import com.kazurayam.materialstore.base.reduce.MaterialProductGroup;
import com.kazurayam.materialstore.base.reduce.zipper.MaterialProduct;
import com.kazurayam.materialstore.core.FileType;
import com.kazurayam.materialstore.core.JobName;
import com.kazurayam.materialstore.core.JobTimestamp;
import com.kazurayam.materialstore.core.Material;
import com.kazurayam.materialstore.core.MaterialList;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.QueryOnMetadata;
import com.kazurayam.materialstore.core.Store;
import com.kazurayam.materialstore.core.Stores;
import com.kazurayam.unittest.TestOutputOrganizer;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GraphNodeIdResolverTest {

    private static final TestOutputOrganizer too =
            TestOutputOrganizerFactory.create(GraphNodeIdResolverTest.class);
    private static final Path issue259Dir =
            too.getProjectDir().resolve("src/test/fixtures/issue#259");
    private static Store store;
    private static JobName jobName;
    private static JobTimestamp leftJobTimestamp;
    private static JobTimestamp rightJobTimestamp;

    @BeforeAll
    public static void beforeAll() throws IOException {
        too.cleanClassOutputDirectory();
        Path root = too.getClassOutputDirectory().resolve("store");
        store = Stores.newInstance(root);
        // copy a fixture into the store
        too.copyDir(issue259Dir.resolve("store"), store.getRoot());
        jobName = new JobName("Main_Twins");
        leftJobTimestamp = new JobTimestamp("20220522_094639");
        rightJobTimestamp = new JobTimestamp("20220522_094706");
    }

    @Test
    public void test_getGraphNodeId_Material_solo() throws MaterialstoreException {
        Material material = store.selectSingle(jobName, leftJobTimestamp, FileType.PNG, QueryOnMetadata.ANY);
        GraphNodeId graphNodeId = GraphNodeIdResolver.resolveIdOfMaterialSolo(material);
        assertEquals(new GraphNodeId("Mf526e62"), graphNodeId);
    }

    @Test
    public void test_getGraphNodeId_Material_in_MaterialList() throws MaterialstoreException {
        MaterialList materialList = store.select(jobName, leftJobTimestamp, FileType.PNG);
        Material material = materialList.get(0);
        GraphNodeId graphNodeId = GraphNodeIdResolver.resolveIdOfMaterialInMaterialList(materialList, material);
        assertEquals(new GraphNodeId("MLe6804ee_Mf526e62"), graphNodeId);
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
                        jobName,
                        reducedTimestamp).build();
        GraphNodeId graphNodeId = GraphNodeIdResolver.resolveIdOfMaterialInMaterialProduct(mProduct, Role.L);
        assertEquals(new GraphNodeId("MPb2096a4_Mf526e62_L"), graphNodeId);
    }

    @Disabled  // https://github.com/kazurayam/materialstore/issues/430
    @Test
    public void test_getGraphNodeId_Material_in_MProductGroup() throws MaterialstoreException {
        MaterialList leftMaterialList = store.select(jobName, leftJobTimestamp);
        MaterialList rightMaterialList = store.select(jobName, rightJobTimestamp);
        MaterialProductGroup mProductGroup =
                new MaterialProductGroup.Builder(leftMaterialList, rightMaterialList)
                        .ignoreKeys("environment", "URL.host")
                        .build();
        assert mProductGroup.size() > 0;
        Material material = leftMaterialList.get(0);
        GraphNodeId graphNodeId = GraphNodeIdResolver.resolveIdOfMaterialInMProductGroup(mProductGroup, material);
        assertEquals(new GraphNodeId("MPG3b23ee9_Md87ac8e"), graphNodeId);
    }

    @Disabled  // https://github.com/kazurayam/materialstore/issues/430
    @Test
    public void test_getGraphNodeId_Material_in_MProductGroup_before_Zip() throws MaterialstoreException {
        MaterialList leftMaterialList = store.select(jobName, leftJobTimestamp, FileType.PNG);
        MaterialList rightMaterialList = store.select(jobName, rightJobTimestamp, FileType.PNG);
        MaterialProductGroup mProductGroup =
                new MaterialProductGroup.Builder(
                        leftMaterialList,
                        rightMaterialList).ignoreKeys("environment", "URL.host").build();
        Material material = leftMaterialList.get(0);
        GraphNodeId graphNodeId = GraphNodeIdResolver.resolveIdOfMaterialInMProductGroupBeforeZIP(mProductGroup, material);
        assertEquals(new GraphNodeId("MPGBZ580d298_MLe6804ee_Mf526e62"), graphNodeId);
    }
}
