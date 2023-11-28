package com.kazurayam.materialstore.base.report;

import com.kazurayam.materialstore.TestOutputOrganizerFactory;
import com.kazurayam.materialstore.core.JobName;
import com.kazurayam.materialstore.core.JobTimestamp;
import com.kazurayam.materialstore.core.MaterialList;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.QueryOnMetadata;
import com.kazurayam.materialstore.core.SortKeys;
import com.kazurayam.materialstore.core.Store;
import com.kazurayam.materialstore.core.Stores;
import com.kazurayam.unittest.TestOutputOrganizer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test 2 classes in sequence.
 * - MaterialListReporterImplMB
 * - MaterialListReporterImpl
 *
 * The MaterialListReportImplMB is the original, which generates HTML using Groovy MarkupBuilder.
 * The MaterialListReportImpl is new, which generates HTML using Java FreeMarker.
 * The test_MaterialListReportImpl depends on the test_MaterialListReportImplMB
 * because it wants to compare 2 HTML files.
 */
public class MaterialListReporterTest extends AbstractReporterTest {

    private static final TestOutputOrganizer too =
            TestOutputOrganizerFactory.create(MaterialListReporterTest.class);
    private final static Path resultsDir =
            too.getProjectDir().resolve("src/test/fixtures/sample_results");
    private static Store store;
    private static Path reportByFreeMarker;

    @BeforeAll
    static void beforeAll() throws IOException {
        too.getClassOutputDirectory();
        Path root = too.getClassOutputDirectory().resolve("store");
        store = Stores.newInstance(root);
        reportByFreeMarker = null;
    }


    @Test
    public void test_MaterialListBasicReporter() throws IOException, MaterialstoreException {
        runMaterialListReporterImpl();
    }

    /**
     *
     */
    void runMaterialListReporterImpl() throws IOException, MaterialstoreException {
        JobName jobName = new JobName("runMaterialListReporterImpl");
        // stuff the Job directory with a fixture
        Path jobNameDir = store.getRoot().resolve(jobName.toString());
        too.copyDir(resultsDir, jobNameDir);
        MaterialListReporter reporter = new MaterialListReporterImpl(store);
        reporter.enablePrettyPrinting(true);
        //
        JobTimestamp jobTimestamp = new JobTimestamp("20210715_145922");
        MaterialList list = store.select(jobName, jobTimestamp,
                QueryOnMetadata.builder()
                        .put("environment", Pattern.compile(".*Env"))
                        .put("category", "page source")
                        .build()
        );
        assertTrue(list.size() > 0, "list is empty");
        String fileName = jobName + "-list.html";

        // generate a MaterialList report by FreeMarker
        SortKeys sortKeys =
                new SortKeys("environment", "URL.protocol", "URL.host",
                        "URL.path", "category", "xpath");
        reportByFreeMarker = reporter.report(list, sortKeys, fileName);
        assertTrue(Files.exists(reportByFreeMarker));
    }

}
