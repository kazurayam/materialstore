package com.kazurayam.materialstore.base.report;

import com.kazurayam.materialstore.base.inspector.Inspector;
import com.kazurayam.materialstore.base.reduce.MaterialProductGroup;
import com.kazurayam.materialstore.TestHelper;
import com.kazurayam.materialstore.core.JobName;
import com.kazurayam.materialstore.core.JobTimestamp;
import com.kazurayam.materialstore.core.MaterialList;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.QueryOnMetadata;
import com.kazurayam.materialstore.core.SortKeys;
import com.kazurayam.materialstore.core.Store;
import com.kazurayam.materialstore.core.Stores;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;

//@Disabled   // https://github.com/kazurayam/materialstore/issues/352
public class MaterialProductGroupReporterTest extends AbstractReporterTest {

    private final Path fixtureDir =
            TestHelper.getFixturesDirectory().resolve("issue#80");

    private Store store;
    private Path report1;

    @BeforeEach
    void setup() throws IOException {
        Path testClassOutputDir = TestHelper.createTestClassOutputDir(MaterialProductGroupReporterTest.class);
        Path root = testClassOutputDir.resolve("store");
        store = Stores.newInstance(root);
        report1 = null;
    }

    @Test
    public void test_MaterialProductGroupBasicReporter() throws IOException, MaterialstoreException {
        runMaterialProductGroupReporterImpl();
    }


    void runMaterialProductGroupReporterImpl() throws IOException, MaterialstoreException {
        JobName jobNameB = new JobName("runMProductGroupReporterImpl");
        //
        MaterialProductGroup reduced = prepareFixture(jobNameB);
        // compile HTML report
        MaterialProductGroupReporterImpl reporter = new MaterialProductGroupReporterImpl(store);
        reporter.enableVerboseLogging(true);
        reporter.enablePrettyPrinting(true);
        reporter.setThreshold(15.0d);
        //
        SortKeys sortKeys = new SortKeys("URL.path");
        report1 = reporter.report(reduced, sortKeys,jobNameB + "-index.html");
        assertTrue(Files.exists(report1));

        // test the report content
        // make sure the HTML contains a string "class='ignored-key'"
        String reportText = readString(report1);
        assertTrue(reportText.contains("class=\"ignored-key\""),
                "expected 'class=\"ignored-key\"' in the report but not found");

        // make sure the HTML contains a string "class='matched-value'"
        assertTrue(reportText.contains("class=\"matched-value\""),
                "expected 'class=\"matched-value\"' in the report but not found");

        // make sure the HTML contains a string "class='identification'"
        assertTrue(reportText.contains("class=\"identification\""),
                "expected a string 'class=\"identification\"' in the report but not found");
    }

    private MaterialProductGroup prepareFixture(JobName jobName) throws IOException, MaterialstoreException {
        // stuff the Job directory with a fixture
        Path jobNameDir = store.getRoot().resolve(jobName.toString());
        FileUtils.copyDirectory(
                fixtureDir.resolve("MyAdmin_visual_inspection_twins").toFile(),
                jobNameDir.toFile());
        //
        JobTimestamp timestamp0 = new JobTimestamp("20220128_191320");
        JobTimestamp timestamp1 = new JobTimestamp("20220128_191342");
        MaterialList left = createMaterialList(jobName, timestamp0, "MyAdmin_ProductionEnv");
        MaterialList right = createMaterialList(jobName, timestamp1, "MyAdmin_DevelopmentEnv");
        Inspector inspector = Inspector.newInstance(store);
        // make diff of the 2 MaterialList objects
        MaterialProductGroup reducedMPG =
                MaterialProductGroup.builder(left, right)
                        .ignoreKeys("environment", "URL.host", "URL.protocol", "URL.port")
                        .identifyWithRegex(
                                Collections.singletonMap(
                                        "URL.query", "\\w{32}"))
                        .labelLeft("ProductionEnv")
                        .labelRight("DevelopmentEnv")
                        .build();
        return inspector.reduceAndSort(reducedMPG);
    }

    private MaterialList createMaterialList(JobName jobName, JobTimestamp timestamp, String profileName)
            throws MaterialstoreException {
        return store.select(jobName, timestamp,
                QueryOnMetadata.builder(
                        Collections.singletonMap("environment", profileName))
                        .build());
    }

}
