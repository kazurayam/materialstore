package issues;

import com.kazurayam.materialstore.base.inspector.Inspector;
import com.kazurayam.materialstore.base.reduce.MaterialProductGroup;
import com.kazurayam.materialstore.base.report.AbstractReporterTest;
import com.kazurayam.materialstore.base.report.MaterialListReporter;
import com.kazurayam.materialstore.base.report.MaterialListReporterImpl;
import com.kazurayam.materialstore.base.report.MaterialProductGroupReporter;
import com.kazurayam.materialstore.base.report.MaterialProductGroupReporterImpl;
import com.kazurayam.materialstore.core.JobName;
import com.kazurayam.materialstore.core.JobTimestamp;
import com.kazurayam.materialstore.core.MaterialList;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.QueryOnMetadata;
import com.kazurayam.materialstore.core.SortKeys;
import com.kazurayam.materialstore.core.Store;
import com.kazurayam.materialstore.core.Stores;
import com.kazurayam.materialstore.zest.TestOutputOrganizerFactory;
import com.kazurayam.unittest.TestOutputOrganizer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Concerning the topic https://github.com/kazurayam/materialstore/issues/449
 *
 * This test will run the following 2 classes:
 * - com.kazurayam.materialstore.base.report.MaterialListReportImpl
 * - com.kazurayam.materialstore.base.report.MaterialProductGroupReporterImpl
 *
 * in order to see if the Apple images have the width appropriately adjusted.
 */
public class Issue449Test extends AbstractReporterTest {

    private static final TestOutputOrganizer too =
            TestOutputOrganizerFactory.create(Issue449Test.class);
    private static final Path fixtureDir =
            too.getProjectDir().resolve("src/test/fixtures/issue#449");
    private static Store store;

    @BeforeAll
    public static void beforeAll() throws IOException {
        too.cleanClassOutputDirectory();
        too.copyDir(fixtureDir, too.getClassOutputDirectory());
        Path storePath = too.getClassOutputDirectory().resolve("store");
        store = Stores.newInstance(storePath);
    }

    @Test
    public void test_report_AppleShootings() throws Exception {
        JobName jobName = new JobName("AppleShootings");
        MaterialListReporter reporter = new MaterialListReporterImpl(store);
        reporter.enablePrettyPrinting(true);
        JobTimestamp jobTimestamp = new JobTimestamp("20231207_230444");
        MaterialList list = store.select(jobName, jobTimestamp, QueryOnMetadata.builder().build());
        String fileName = jobName + "-list.html";
        SortKeys sortKeys = new SortKeys("step");
        Path report = reporter.report(list, sortKeys, fileName);
        //
        assertTrue(Files.exists(report));
        assertTrue(Files.exists(report));
        String reportText = readString(report);
        assertTrue(reportText.contains("function adjustImgWidth()"),
                "the report text does not contain the function adjustImgWidth() declaration");
    }

    @Test
    public void test_report_AppleTwinsDiff() throws Exception {
        JobName jobName = new JobName("AppleTwinsDiff");
        MaterialProductGroup reduced = prepareFixture(jobName);
        // compile HTML report
        MaterialProductGroupReporter reporter = new MaterialProductGroupReporterImpl(store);
        reporter.enablePrettyPrinting(true);
        reporter.setThreshold(15.0d);
        //
        SortKeys sortKeys = new SortKeys("step");
        Path report = reporter.report(reduced, sortKeys, jobName + "-index.html");
        //
        assertTrue(Files.exists(report));
        String reportText = readString(report);
        assertTrue(reportText.contains("function adjustImgWidth()"),
                "the report text does not contain the function adjustImgWidth() declaration");
    }


    private MaterialProductGroup prepareFixture(JobName jobName) throws IOException, MaterialstoreException {
        Path jobNameDir = store.getRoot().resolve(jobName.toString());
        JobTimestamp timestamp0 = new JobTimestamp("20231207_230425");
        JobTimestamp timestamp1 = new JobTimestamp("20231207_230429");
        MaterialList left = createMaterialList(jobName, timestamp0, "AppleTwinsDiff_ProductionEnv");
        MaterialList right = createMaterialList(jobName, timestamp1, "AppleTwinsDiff_DevelopmentEnv");
        Inspector inspector = Inspector.newInstance(store);
        MaterialProductGroup mpg =
                MaterialProductGroup.builder(left, right)
                        .ignoreKeys("environment", "URL.host", "URL.protocol", "URL.poart", "URL.path", "image-width", "image-height")
                        .labelLeft("ProductionEnv")
                        .labelRight("DevelopmentEnv")
                        .build();
        return inspector.reduceAndSort(mpg);
    }

    private MaterialList createMaterialList(JobName jobName, JobTimestamp timestamp, String profileName)
            throws MaterialstoreException {
        return store.select(jobName, timestamp,
                QueryOnMetadata.builder().build());
    }
}
