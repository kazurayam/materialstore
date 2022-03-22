package com.kazurayam.materialstore.report;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.Patch;
import com.kazurayam.materialstore.Inspector;
import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.TextDiffUtil;
import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.filesystem.QueryOnMetadata;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.filesystem.Stores;
import com.kazurayam.materialstore.reduce.MProductGroup;
import com.kazurayam.materialstore.util.StringUtils;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MProductGroupReporterTest extends AbstractReporterTest {

    private static final Path fixtureDir =
            Paths.get(".").resolve("src/test/fixture/issue#80");

    private static final Path testOutput =
            Paths.get(".").resolve("build/tmp/testOutput");

    private final static Path outputDir =
            testOutput.resolve(MProductGroupReporterTest.class.getName());

    private static Store store;
    private static JobName jobNameA;
    private static Path report0;
    private static Path report1;

    @BeforeAll
    static void beforeAll() throws IOException {
        if (Files.exists(outputDir)) {
            // make sure the outputDir to be empty
            FileUtils.deleteDirectory(outputDir.toFile());
        }
        Files.createDirectories(outputDir);
        Path root = outputDir.resolve("store");
        store = Stores.newInstance(root);
        report0 = null;
        report1 = null;
    }

    @Test
    public void test_MProductGroupBasicReporter() throws IOException, MaterialstoreException {
        runMProductGroupReporterImplMB();
        runMProductGroupReporterImpl();
    }


    void runMProductGroupReporterImplMB() throws IOException, MaterialstoreException {
        jobNameA = new JobName("runMProductGroupReporterImplMB");
        //
        MProductGroup reduced = prepareFixture(jobNameA);
        // compile HTML report
        MProductGroupReporter reporter = new MProductGroupReporterImplMB(store, jobNameA);
        reporter.enableVerboseLogging(true);
        reporter.enablePrettyPrinting(true);

        // generate the HTML report file
        report0 = reporter.report(reduced, jobNameA + "-index.html");
        assertTrue(Files.exists(report0));

        // test the report content
        String reportText = StringUtils.join(Files.readAllLines(report0));
        // make sure the HTML contains a string "class='ignored-key'"
        assertTrue(reportText.contains("class=\"ignored-key\""),
                "expected class=\"ignored-key\" in the report but not found");

        // make sure the HTML contains a string "class='matched-value'"
        assertTrue(reportText.contains("class=\"matched-value\""),
                "expected class=\"matched-value\" in the report but not found");

        // make sure the HTML contains a string "class='identified-value'"
        assertTrue(reportText.contains("class=\"identified-value\""),
                "expected a string class=\"identified-value\" in the report but not found");

        //  issue#86:
        // make sure the HTML contains
        //     <span>"/npm/bootstrap-icons@</span><span class='semantic-version'>1.5.0</span><span>/font/bootstrap-icons.css"</span>
        // for short,
        // a string "<span class='semantic-version'>1." and
        // a string ".0</span>"
        String s1 = "<span class=\"semantic-version\">1.";
        String s2 = ".0</span>";
        assertTrue(reportText.contains(s1) && reportText.contains(s2),
                String.format("expected \"%s\" and \"%s\" in the report but not found", s1, s2));
    }

    void runMProductGroupReporterImpl() throws IOException, MaterialstoreException {
        JobName jobNameB = new JobName("runMProductGroupReporterImpl");
        //
        MProductGroup reduced = prepareFixture(jobNameB);
        // compile HTML report
        MProductGroupReporterImpl reporter = new MProductGroupReporterImpl(store, jobNameB);
        reporter.enableVerboseLogging(true);
        reporter.enablePrettyPrinting(true);
        //
        report1 = reporter.report(reduced, jobNameB + "-index.html");
        assertTrue(Files.exists(report1));
        // make diff of 2 report files using java-diff-utils to compare them
        List<String> original = trimLines(Files.readAllLines(report0));
        List<String> revised = trimLines(Files.readAllLines(report1));
        Path diff = outputDir.resolve("store").resolve("diff.md");
        TextDiffUtil.writeDiff(original, revised, diff,
                Arrays.asList(jobNameA.toString(), jobNameB.toString()));
        // compute the patch, display into the console
        Patch<String> patch = DiffUtils.diff(original, revised);
        //patch.getDeltas().forEach(System.out::println);

        // test the report content
        // make sure the HTML contains a string "class='ignored-key'"
        String reportText = readString(report1);
        assertTrue(reportText.contains("class=\"ignored-key\""),
                "expected 'class=\"ignored-key\"' in the report but not found");

        // make sure the HTML contains a string "class='matched-value'"
        assertTrue(reportText.contains("class=\"matched-value\""),
                "expected 'class=\"matched-value\"' in the report but not found");

        // make sure the HTML contains a string "class='identified-value'"
        assertTrue(reportText.contains("class=\"identified-value\""),
                "expected a string 'class=\"identified-value\"' in the report but not found");
    }

    private MProductGroup prepareFixture(JobName jobName) throws IOException {
        // stuff the Job directory with a fixture
        Path jobNameDir = store.getRoot().resolve(jobName.toString());
        FileUtils.copyDirectory(
                fixtureDir.resolve("MyAdmin_Visual_inspection_twins").toFile(),
                jobNameDir.toFile());
        //
        JobTimestamp timestamp0 = new JobTimestamp("20220128_191320");
        JobTimestamp timestamp1 = new JobTimestamp("20220128_191342");
        MaterialList left = createMaterialList(jobName, timestamp0, "MyAdmin_ProductionEnv");
        MaterialList right = createMaterialList(jobName, timestamp1, "MyAdmin_DevelopmentEnv");
        Inspector inspector = Inspector.newInstance(store);
        // make diff of the 2 MaterialList objects
        MProductGroup prepared =
                MProductGroup.builder(left, right)
                        .ignoreKeys("profile", "URL", "URL.host")
                        .identifyWithRegex(
                                Collections.singletonMap(
                                        "URL.query", "\\w{32}"))
                        .build();
        return inspector.reduce(prepared);
    }

    private MaterialList createMaterialList(JobName jobName, JobTimestamp timestamp, String profileName) {
        return store.select(jobName, timestamp,
                QueryOnMetadata.builder(
                        Collections.singletonMap("profile", profileName))
                        .build());
    }

}
