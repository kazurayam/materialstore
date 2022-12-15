package com.kazurayam.materialstore.diagram.dot;

import com.kazurayam.materialstore.base.inspector.Inspector;
import com.kazurayam.materialstore.base.reduce.MaterialProductGroup;
import com.kazurayam.materialstore.base.reduce.zipper.MaterialProduct;
import com.kazurayam.materialstore.core.filesystem.JobName;
import com.kazurayam.materialstore.core.filesystem.JobTimestamp;
import com.kazurayam.materialstore.core.filesystem.MaterialList;
import com.kazurayam.materialstore.core.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.core.filesystem.QueryOnMetadata;
import com.kazurayam.materialstore.core.filesystem.Store;
import com.kazurayam.materialstore.core.filesystem.Stores;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This test takes is tentatively disabled
 * because it takes long time (over 1 minutes 20 seconds)
 */
@Disabled
public class QueryOnMetadataAsGraphNodeTest {

    private static final Path outputDir =
            Paths.get(System.getProperty("user.dir"))
                    .resolve("build/tmp/testOutput")
                    .resolve(DotGeneratorTest.class.getName());

    private static final Path issue259Dir =
            Paths.get(".")
                    .resolve("src/test/fixtures/issue#259");

    private static Store store;
    private static JobName jobName;
    private static JobTimestamp leftTimestamp;
    private static JobTimestamp rightTimestamp;

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