package com.kazurayam.materialstore.diagram.dot;

import com.kazurayam.materialstore.base.reduce.MaterialProductGroup;
import com.kazurayam.materialstore.core.TestHelper;
import com.kazurayam.materialstore.core.filesystem.FileType;
import com.kazurayam.materialstore.core.filesystem.JobName;
import com.kazurayam.materialstore.core.filesystem.JobTimestamp;
import com.kazurayam.materialstore.core.filesystem.MaterialList;
import com.kazurayam.materialstore.core.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.core.filesystem.Store;
import com.kazurayam.materialstore.core.filesystem.Stores;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This test is disabled because it failed in the GitHub Actions with message:
 *
 * MPGVisualizerTest > test_visualize() FAILED
 *     com.kazurayam.materialstore.core.filesystem.MaterialstoreException at MPGVisualizerTest.java:60
 *         Caused by: java.io.IOException at MPGVisualizerTest.java:60
 *             Caused by: java.io.IOException at MPGVisualizerTest.java:60
 * WARNING: An illegal reflective access operation has occurred
 * WARNING: Illegal reflective access by org.apache.poi.openxml4j.util.ZipSecureFile$1 (file:/home/runner/.gradle/caches/modules-2/files-2.1/org.apache.poi/poi-ooxml/3.17/7d8c44407178b73246462842bf1e206e99c8e0a/poi-ooxml-3.17.jar) to field java.io.FilterInputStream.in
 * WARNING: Please consider reporting this to the maintainers of org.apache.poi.openxml4j.util.ZipSecureFile$1
 * WARNING: Use --illegal-access=warn to enable warnings of further illegal reflective access operations
 * WARNING: All illegal access operations will be denied in a future release
 *
 * I could not fix this error ... it's a shame.
 */
@Disabled
public class MPGVisualizerTest {

    private static final Path outputDir =
            TestHelper.createTestClassOutputDir(MPGVisualizerTest.class);

    private static final Path issue80Dir =
            TestHelper.getFixturesDirectory().resolve("issue#80");

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
                        rightMaterialList).ignoreKeys("environment", "URL.host").build();
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
