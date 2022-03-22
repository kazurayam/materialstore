package com.kazurayam.materialstore.report;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.Patch;
import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.TextDiffUtil;
import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.MaterialList;
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
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test 2 classes in sequence.
 * - MaterialListBasicReporter
 * - MaterialListBasicReporterFM
 *
 * The MaterialListBasicReport is the original, which generates HTML using Groovy MarkupBuilder.
 * The MaterialListBasicReportFM is new, which generates HTML using Java FreeMarker.
 * The test_MaterialListBasicReportFM depends on the test_MaterialListBasicReport
 * because it wants to compare 2 HTML files.
 */
public class MaterialListReporterTest extends AbstractReporterTest {

    private final static Path outputDir =
            Paths.get(".").resolve("build/tmp/testOutput")
                    .resolve(MaterialListReporterTest.class.getName());

    private final static Path resultsDir =
            Paths.get(".").resolve("src/test/fixture/sample_results");

    private static Store store;

    private static Path reportByMarkupBuilder;
    private static Path reportByFreeMarker;

    @BeforeAll
    static void beforeAll() throws IOException {
        if (Files.exists(outputDir)) {
            // make sure the outputDir to be empty
            FileUtils.deleteDirectory(outputDir.toFile());
        }
        Files.createDirectories(outputDir);
        Path root = outputDir.resolve("store");
        store = Stores.newInstance(root);
        reportByMarkupBuilder = null;
        reportByFreeMarker = null;
    }

    @Test
    public void test_MaterialListBasicReporter() throws IOException, MaterialstoreException {
        runMaterialListBasicReport();
    }

    @Test
    public void test_MaterialListBasicReporterFM() throws IOException, MaterialstoreException {
        runMaterialListBasicReport();
        runMaterialListBasicReportFM();
    }

    /**
     *
     */
    void runMaterialListBasicReport() throws IOException, MaterialstoreException {
        JobName jobName = new JobName("runMaterialListBasicReport");
        // stuff the Job directory with a fixture
        Path jobNameDir = store.getRoot().resolve(jobName.toString());
        FileUtils.copyDirectory(resultsDir.toFile(), jobNameDir.toFile());
        //
        MaterialListReporter reporter =
                new MaterialListBasicReporter(store, jobName);
        reporter.enablePrettyPrinting(true);
        JobTimestamp jobTimestamp = new JobTimestamp("20210715_145922");
        MaterialList list = store.select(jobName, jobTimestamp,
                QueryOnMetadata.builder()
                        .put("profile", Pattern.compile(".*Env"))
                        .put("category", "page source")
                        .build());
        assertTrue(list.size() > 0, "list is empty");
        String fileName = jobName + "-list.html";
        reportByMarkupBuilder = reporter.report(list, fileName);
        assertNotNull(reportByMarkupBuilder);
        assertTrue(Files.exists(reportByMarkupBuilder));
    }


    /**
     *
     */
    void runMaterialListBasicReportFM() throws IOException, MaterialstoreException {
        JobName jobName = new JobName("runMaterialListBasicReportFM");
        // stuff the Job directory with a fixture
        Path jobNameDir = store.getRoot().resolve(jobName.toString());
        FileUtils.copyDirectory(resultsDir.toFile(), jobNameDir.toFile());
        MaterialListReporter reporter =
                new MaterialListBasicReporterFM(store, jobName);
        reporter.enablePrettyPrinting(true);
        //
        JobTimestamp jobTimestamp = new JobTimestamp("20210715_145922");
        MaterialList list = store.select(jobName, jobTimestamp,
                QueryOnMetadata.builder()
                        .put("profile", Pattern.compile(".*Env"))
                        .put("category", "page source")
                        .build()
        );
        assertTrue(list.size() > 0, "list is empty");
        String fileName = jobName + "-list.html";
        reportByFreeMarker = reporter.report(list, fileName);
        assertTrue(Files.exists(reportByFreeMarker));


        // check if the report is 100% identical to the one generated by Groovy MarkupBuilder.
        assert Files.exists(reportByMarkupBuilder);
        // compare 2 report files using java-diff-utils
        List<String> original = trimLines(Files.readAllLines(reportByMarkupBuilder));
        List<String> revised = trimLines(Files.readAllLines(reportByFreeMarker));
        Path diff = outputDir.resolve("store").resolve("diff.md");
        TextDiffUtil.writeDiff(original, revised, diff,
                Arrays.asList(
                        "runMaterialListBasicReport",
                        "runMaterialListBasicReportFM"
                ));
        //compute the patch: this is the diffutils part
        Patch<String> patch = DiffUtils.diff(original, revised);
        patch.getDeltas().forEach(System.out::println);
        assertEquals(5, patch.getDeltas().size());
    }


}
