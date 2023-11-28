package com.kazurayam.materialstore.diagram.dot;

import com.kazurayam.materialstore.TestOutputOrganizerFactory;
import com.kazurayam.materialstore.base.inspector.Inspector;
import com.kazurayam.materialstore.base.reduce.MaterialProductGroup;
import com.kazurayam.materialstore.base.reduce.zipper.MaterialProduct;
import com.kazurayam.materialstore.core.FileType;
import com.kazurayam.materialstore.core.JobName;
import com.kazurayam.materialstore.core.JobTimestamp;
import com.kazurayam.materialstore.core.Material;
import com.kazurayam.materialstore.core.MaterialList;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.Store;
import com.kazurayam.materialstore.core.Stores;
import com.kazurayam.unittest.TestOutputOrganizer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This test takes is tentatively disabled
 * because it takes long time (over 1 minutes 20 seconds)
 */
@Disabled
public class MaterialAsGraphNodeTest {

    private static Logger logger_ = LoggerFactory.getLogger(MaterialAsGraphNodeTest.class);
    private static TestOutputOrganizer too =
            TestOutputOrganizerFactory.create(MaterialAsGraphNodeTest.class);
    private static final Path issue259Dir =
            too.getProjectDir().resolve("src/test/fixtures/issue#259");

    private static Store store;
    private static JobName jobName;
    private static JobTimestamp leftTimestamp;
    private static JobTimestamp rightTimestamp;

    @BeforeAll
    public static void beforeAll() throws IOException {
        too.cleanClassOutputDirectory();
        Path root = too.getClassOutputDirectory().resolve("store");
        store = Stores.newInstance(root);
        // copy a fixture into the store
        too.copyDir(issue259Dir.resolve("store"), store.getRoot());
        jobName = new JobName("Main_Twins");
        leftTimestamp = new JobTimestamp("20220522_094639");
        rightTimestamp = new JobTimestamp("20220522_094706");
    }

    @Disabled
    @Test
    public void test_generateDot_Material() throws MaterialstoreException {
        JobTimestamp fixtureTimestamp = new JobTimestamp("20220522_094639");
        Material material = store.selectSingle(jobName, fixtureTimestamp, FileType.PNG);
        GraphNodeId graphNodeId = new GraphNodeId("M0123456");
        MaterialAsGraphNode materialNode = new MaterialAsGraphNode(graphNodeId, material);
        assertTrue(materialNode.toGraphNode().startsWith(graphNodeId.getValue()));
    }

    @Test
    public void test_formatMetadata() throws MaterialstoreException {
        MaterialList leftMaterialList = store.select(jobName, leftTimestamp);
        MaterialList rightMaterialList = store.select(jobName, rightTimestamp);
        //JobTimestamp reducedTimestamp = JobTimestamp.now();
        //JobName outJobName = new JobName("test_formatMetadata");
        //JobTimestamp outJobTimestamp = JobTimestamp.laterThan(reducedTimestamp);
        MaterialProductGroup reduced =
                new MaterialProductGroup.Builder(
                        leftMaterialList,
                        rightMaterialList)
                        .ignoreKeys("environment", "URL.host")
                        .identifyWithRegex(
                                Collections.singletonMap("URL.query", "\\w{32}")
                        )
                        .build();
        assert reduced.size() > 0;
        //
        Inspector inspector = Inspector.newInstance(store);
        MaterialProductGroup inspected = inspector.reduceAndSort(reduced);
        //
        MaterialProduct mp = inspected.get(4);
        Material material = mp.getLeft();
        logger_.info("[test_formatMetadata] material.getMetadata().toJson(true)=\n"
                + material.getMetadata().toJson(true));
        GraphNodeId graphNodeId = new GraphNodeId("M0123456");
        MaterialAsGraphNode materialNode = new MaterialAsGraphNode(graphNodeId, material);
        String htmlContent = materialNode.formatMetadata();
        logger_.info("[test_formatMetadata] htmlContent=\n" + htmlContent);
        //assertEquals("{<S>&quot;", htmlContent.substring(0, htmlContent.indexOf("URL")));
    }


    @Disabled
    @Test
    public void test_look_at_Material_inside_MProductGroup_after_zipping() throws MaterialstoreException {
        MaterialList leftMaterialList = store.select(jobName, leftTimestamp);
        MaterialList rightMaterialList = store.select(jobName, rightTimestamp);
        JobTimestamp reducedTimestamp = JobTimestamp.now();
        JobName outJobName = new JobName("test_look_at_Material_inside_MProductGroup_after_zipping");
        JobTimestamp outJobTimestamp = JobTimestamp.laterThan(reducedTimestamp);
        MaterialProductGroup reduced =
                new MaterialProductGroup.Builder(
                        leftMaterialList,
                        rightMaterialList)
                        .ignoreKeys("environment", "URL.host")
                        .identifyWithRegex(
                                Collections.singletonMap("URL.query","\\w{32}")
                        )
                        .build();
        assert reduced.size() > 0;
        //
        Inspector inspector = Inspector.newInstance(store);
        MaterialProductGroup inspected = inspector.reduceAndSort(reduced);
        //
        logger_.info("[test_look_at_Material_inside_MProductGroup_after_zipping]\n" + inspected.toJson(true));
        // see the file "./inspected_MProductGroup_sample.json" for the output recorded
    }
}