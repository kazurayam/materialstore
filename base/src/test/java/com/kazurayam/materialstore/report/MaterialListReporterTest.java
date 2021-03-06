package com.kazurayam.materialstore.report;

import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.filesystem.MaterialstoreException;
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

    private final static Path outputDir =
            Paths.get(".").resolve("build/tmp/testOutput")
                    .resolve(MaterialListReporterTest.class.getName());

    private final static Path resultsDir =
            Paths.get(".").resolve("src/test/fixture/sample_results");

    private static Store store;

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
        FileUtils.copyDirectory(resultsDir.toFile(), jobNameDir.toFile());
        MaterialListReporter reporter =
                new MaterialListReporterImpl(store, jobName);
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
        // generate a MaterialList report by FreeMarker
        reportByFreeMarker = reporter.report(list, fileName);
        assertTrue(Files.exists(reportByFreeMarker));
    }

}
