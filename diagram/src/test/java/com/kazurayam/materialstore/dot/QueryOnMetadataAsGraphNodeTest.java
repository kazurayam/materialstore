package com.kazurayam.materialstore.dot;

import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.QueryOnMetadata;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.filesystem.Stores;
import com.kazurayam.materialstore.inspector.Inspector;
import com.kazurayam.materialstore.reduce.MaterialProductGroup;
import com.kazurayam.materialstore.reduce.zipper.MaterialProduct;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class QueryOnMetadataAsGraphNodeTest {

    private static final Path outputDir =
            Paths.get(System.getProperty("user.dir"))
                    .resolve("build/tmp/testOutput")
                    .resolve(DotGeneratorTest.class.getName());

    private static final Path issue259Dir =
            Paths.get(".")
                    .resolve("src/test/fixture/issue#259");

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
                        .ignoreKeys("profile", "URL.host")
                        .identifyWithRegex(
                                Collections.singletonMap("URL.query", "\\w{32}")
                        )
                        .build();
        assert reduced.size() > 0;
        //
        Inspector inspector = Inspector.newInstance(store);
        MaterialProductGroup inspected = inspector.process(reduced);
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
