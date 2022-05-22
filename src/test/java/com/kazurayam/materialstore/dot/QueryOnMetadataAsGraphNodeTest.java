package com.kazurayam.materialstore.dot;

import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.Metadata;
import com.kazurayam.materialstore.filesystem.QueryOnMetadata;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.filesystem.Stores;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QueryOnMetadataAsGraphNodeTest {

    @Test
    public void test_toGraphNode() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(2);
        map.put("profile", "ProjectionEnv");
        map.put("category", "screenshot");
        Metadata metadata = Metadata.builder(map).build();
        QueryOnMetadata query = QueryOnMetadata.builder(metadata).build();
        GraphNodeId leftMaterial = new GraphNodeId("M0123456");
        GraphNodeId rightMaterial = new GraphNodeId("M9876543");
        QueryOnMetadataAsGraphNode queryAsNode = new QueryOnMetadataAsGraphNode(query, leftMaterial, rightMaterial);
        String statement = queryAsNode.toGraphNode();
        System.out.println(statement);
        assertTrue(statement.startsWith("QUERY_"));
    }
}
