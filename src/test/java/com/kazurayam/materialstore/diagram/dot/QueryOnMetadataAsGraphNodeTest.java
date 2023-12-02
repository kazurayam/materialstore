package com.kazurayam.materialstore.diagram.dot;

import com.kazurayam.materialstore.zest.TestOutputOrganizerFactory;
import com.kazurayam.materialstore.base.inspector.Inspector;
import com.kazurayam.materialstore.base.reduce.MaterialProductGroup;
import com.kazurayam.materialstore.base.reduce.zipper.MaterialProduct;
import com.kazurayam.materialstore.core.JobName;
import com.kazurayam.materialstore.core.JobTimestamp;
import com.kazurayam.materialstore.core.MaterialList;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.QueryOnMetadata;
import com.kazurayam.materialstore.core.Store;
import com.kazurayam.materialstore.core.Stores;
import com.kazurayam.unittest.TestOutputOrganizer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This test takes is tentatively disabled
 * because it takes long time (over 1 minutes 20 seconds)
 */
@Disabled
public class QueryOnMetadataAsGraphNodeTest {

    private static final TestOutputOrganizer too =
            TestOutputOrganizerFactory.create(QueryOnMetadataAsGraphNodeTest.class);
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

    @Test
    public void test_toGraphNode() throws MaterialstoreException {
        MaterialList leftMaterialList = store.select(jobName, leftTimestamp);
        MaterialList rightMaterialList = store.select(jobName, rightTimestamp);
        JobTimestamp reducedTimestamp = JobTimestamp.now();
        // save the dot file and the PNG image into the store directory
        JobName outJobName = new JobName("test_generateDot_MProductGroup");
        JobTimestamp outJobTimestamp = JobTimestamp.laterThan(reducedTimestamp);
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
        MaterialProduct materialProduct = inspected.get(0);
        GraphNodeId nodeId = GraphNodeIdResolver.resolveIdOfQueryInMaterialProduct(materialProduct);
        QueryOnMetadata query = materialProduct.getQueryOnMetadata();
        QueryAsGraphNode queryAsGraphNode = new QueryAsGraphNode(nodeId, query);
        //
        String nodeStatement = queryAsGraphNode.toGraphNode();
        //assertEquals("foo", nodeStatement);
        assertTrue(nodeStatement.substring(0,11).matches("MP\\w{7}_Q"));
    }
}
