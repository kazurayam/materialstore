package issues;

import com.kazurayam.materialstore.base.inspector.Inspector;
import com.kazurayam.materialstore.base.reduce.MaterialProductGroup;
import com.kazurayam.materialstore.base.report.AbstractReporterTest;
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

public class Issue450Test  extends AbstractReporterTest {

    private static final TestOutputOrganizer too =
            TestOutputOrganizerFactory.create(Issue450Test.class);
    private static final Path fixtureDir =
            too.getProjectDir().resolve("src/test/fixtures/issue#450");
    private static Store store;

    @BeforeAll
    public static void beforeAll() throws IOException {
        too.cleanClassOutputDirectory();
        too.copyDir(fixtureDir, too.getClassOutputDirectory());
        Path storePath = too.getClassOutputDirectory().resolve("store");
        store = Stores.newInstance(storePath);
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
        JobTimestamp timestamp0 = new JobTimestamp("20231208_203823");
        JobTimestamp timestamp1 = new JobTimestamp("20231208_203835");
        MaterialList left = createMaterialList(jobName, timestamp0, "AppleTwinsDiff_ProductionEnv");
        MaterialList right = createMaterialList(jobName, timestamp1, "AppleTwinsDiff_DevelopmentEnv");
        Inspector inspector = Inspector.newInstance(store);
        MaterialProductGroup mpg =
                MaterialProductGroup.builder(left, right)
                        .ignoreKeys("environment", "URL.host", "URL.protocol", "URL.port", "URL.path", "image-width", "image-height")
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
