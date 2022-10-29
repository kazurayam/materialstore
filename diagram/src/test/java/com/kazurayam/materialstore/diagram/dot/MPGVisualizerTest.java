package com.kazurayam.materialstore.diagram.dot;

import com.kazurayam.materialstore.core.filesystem.FileType;
import com.kazurayam.materialstore.core.filesystem.JobName;
import com.kazurayam.materialstore.core.filesystem.JobTimestamp;
import com.kazurayam.materialstore.core.filesystem.MaterialList;
import com.kazurayam.materialstore.core.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.core.filesystem.Store;
import com.kazurayam.materialstore.core.filesystem.Stores;
import com.kazurayam.materialstore.base.reduce.MaterialProductGroup;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MPGVisualizerTest {

    private static final Path outputDir =
            Paths.get(System.getProperty("user.dir"))
                    .resolve("build/tmp/testOutput")
                    .resolve(MPGVisualizerTest.class.getName());

    private static final Path issue80Dir =
            Paths.get(".")
                    .resolve("src/test/fixtures/issue#80");

    private static Store store;
    private static JobName jobName;
    JobTimestamp leftTimestamp = new JobTimestamp("20220128_191320");
    JobTimestamp rightTimestamp = new JobTimestamp("20220128_191342");

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
    }

    @Test
    public void test_visualize() throws MaterialstoreException {
        MaterialList leftMaterialList = store.select(jobName, leftTimestamp);
        MaterialList rightMaterialList = store.select(jobName, rightTimestamp);
        MaterialProductGroup mProductGroup =
                new MaterialProductGroup.Builder(
                        leftMaterialList,
                        rightMaterialList).ignoreKeys("profile", "URL.host").build();
        //
        MPGVisualizer visualizer = new MPGVisualizer(store);
        JobTimestamp jobTimestamp = JobTimestamp.now();
        visualizer.visualize(jobName, jobTimestamp, mProductGroup);
        //
        MaterialList dots = store.select(jobName, jobTimestamp, FileType.DOT);
        assertEquals(2, dots.size());
        MaterialList pngs = store.select(jobName, jobTimestamp, FileType.PNG);
        assertEquals(2, pngs.size());
    }
}
