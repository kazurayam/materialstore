package com.kazurayam.materialstore.report;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;
import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.TextDiffUtil;
import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.filesystem.Stores;
import com.kazurayam.materialstore.filesystem.QueryOnMetadata;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MaterialListBasicReportFM class is developed in Java, not in Groovy
 *
 * MaterialListBasicReportFM uses Free Marker as the HTML template engine.
 *
 */
public class MaterialListBasicReporterFMTest {

    private static final Logger logger = LoggerFactory.getLogger(MaterialListBasicReporterFMTest.class);

    private static Path testOutput =
            Paths.get(".").resolve("build/tmp/testOutput");

    private static Path outputDir =
            testOutput.resolve(MaterialListBasicReporterFMTest.class.getName());

    private static Path resultsDir =
            Paths.get(".").resolve("src/test/fixture/sample_results");

    @BeforeAll
    static void beforeAll() throws IOException {
        if (Files.exists(outputDir)) {
            deleteDirectory(outputDir.toFile());
        }
        Files.createDirectories(outputDir);
    }

    @Test
    void test_report() throws IOException, MaterialstoreException {
        Path root = outputDir.resolve("store");
        Store store = Stores.newInstance(root);
        JobName jobName = new JobName("test_report");
        // make sure the Job directory to be empty
        FileUtils.deleteDirectory(root.resolve(jobName.toString()).toFile());
        // stuff the Job directory with a fixture
        Path jobNameDir = root.resolve(jobName.toString());
        FileUtils.copyDirectory(resultsDir.toFile(), jobNameDir.toFile());
        //
        MaterialListReporter reporter =
                new MaterialListBasicReporterFM(store, jobName);
        JobTimestamp jobTimestamp = new JobTimestamp("20210715_145922");
        MaterialList list = store.select(jobName, jobTimestamp,
                QueryOnMetadata.builder()
                        .put("profile", Pattern.compile(".*Env")).build()
        );
        assertTrue(list.size() > 0, "list is empty");
        String fileName = "test_report-listFM.html";
        Path report = reporter.report(list, fileName);
        //
        assertTrue(Files.exists(report));

        // check if the report is 100% identical to the on generated by Groovy MarkupBuilder.
        Path reportByMarkupBuilder =
                testOutput.resolve(MaterialListBasicReporterTest.class.getName())
                        .resolve("store/test_report-list.html");
        assert Files.exists(reportByMarkupBuilder);

        // using java-diff-utils, compare 2 report files
        List<String> original = Files.readAllLines(reportByMarkupBuilder);
        List<String> revised = Files.readAllLines(report);
        Path diff = outputDir.resolve("store").resolve("diff.md");
        TextDiffUtil.writeDiff(original, revised, diff,
                Arrays.asList("test_report-",
                        "MaterialListBasicReporterFMTest"));

        //compute the patch: this is the diffutils part
        Patch<String> patch = DiffUtils.diff(original, revised);
        assertEquals(0, patch.getDeltas().size());
    }
}