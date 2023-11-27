package issues;

import com.kazurayam.materialstore.TestOutputOrganizerFactory;
import com.kazurayam.materialstore.base.inspector.Inspector;
import com.kazurayam.materialstore.base.reduce.MaterialProductGroup;
import com.kazurayam.materialstore.core.JobName;
import com.kazurayam.materialstore.core.JobTimestamp;
import com.kazurayam.materialstore.core.MaterialList;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.QueryOnMetadata;
import com.kazurayam.materialstore.core.Store;
import com.kazurayam.materialstore.core.Stores;
import com.kazurayam.unittest.TestOutputOrganizer;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Reproducing the issue #73 at https://github.com/kazurayam/materialstore/issues/73
 * and fixing it.
 */
@Disabled
public class Issue73Test {

    private static final TestOutputOrganizer too =
            TestOutputOrganizerFactory.create(Issue73Test.class);
    private static final Path fixtureDir =
            too.getProjectDir().resolve("src/test/fixtures/issue#73");
    private static Store store;
    private static final JobName jobName = new JobName("MyAdmin_visual_inspection_twins");
    private static final JobTimestamp timestampP = new JobTimestamp("20220125_140449");
    private static final JobTimestamp timestampD = new JobTimestamp("20220125_140509");
    private static final String leftUrl = "https://cdn.jsdelivr.net/npm/bootstrap@5.1.0/dist/js/bootstrap.bundle.min.js";
    private static final String rightUrl = "https://cdn.jsdelivr.net/npm/bootstrap@5.1.3-rc1/dist/js/bootstrap.bundle.min.js";
    private MaterialList left;
    private MaterialList right;

    @BeforeAll
    public static void beforeAll() throws IOException {
        too.cleanClassOutputDirectory();
        Path storePath = too.getClassOutputDirectory().resolve("store");
        too.copyDir(fixtureDir, storePath);
        store = Stores.newInstance(storePath);
    }

    @BeforeEach
    public void beforeEach() throws MaterialstoreException {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>(1);
        map.put("environment", "MyAdmin_ProductionEnv");
        left = store.select(jobName, timestampP, QueryOnMetadata.builder(map).build());
        assert left.size() == 8;
        LinkedHashMap<String, String> map1 = new LinkedHashMap<String, String>(1);
        map1.put("environment", "MyAdmin_DevelopmentEnv");
        right = store.select(jobName, timestampD, QueryOnMetadata.builder(map1).build());
        assert right.size() == 8;
    }

    @Test
    public void test_smoke() throws MaterialstoreException {
        Inspector inspector = Inspector.newInstance(store);
        MaterialProductGroup reducedMPG = MaterialProductGroup.builder(left, right).ignoreKeys("environment", "URL.host").build();
        MaterialProductGroup processedMPG = inspector.reduceAndSort(reducedMPG);
        Double threshold = 0.0d;
        int warnings = processedMPG.countWarnings(threshold);
        // compile the report
        Path reportFile = inspector.report(processedMPG, threshold);
        assertEquals(8, processedMPG.size());
    }
}
