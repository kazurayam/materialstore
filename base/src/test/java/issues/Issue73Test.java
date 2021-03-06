package issues;

import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.QueryOnMetadata;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.filesystem.Stores;
import com.kazurayam.materialstore.inspector.Inspector;
import com.kazurayam.materialstore.reduce.MProductGroup;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;

/**
 * Reproducing the issue #73 at https://github.com/kazurayam/materialstore/issues/73
 * and fixing it.
 */
public class Issue73Test {


    private static final Path fixtureDir = Paths.get(".").resolve("src/test/fixture/issue#73");
    private static final Path outputDir = Paths.get(".").resolve("build/tmp/testOutput").resolve(Issue73Test.class.getName());
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
        if (Files.exists(outputDir)) {
            FileUtils.deleteDirectory(outputDir.toFile());
        }

        Files.createDirectories(outputDir);
        Path storePath = outputDir.resolve("store");
        FileUtils.copyDirectory(fixtureDir.toFile(), storePath.toFile());
        store = Stores.newInstance(storePath);
    }

    @BeforeEach
    public void beforeEach() throws MaterialstoreException {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>(1);
        map.put("profile", "MyAdmin_ProductionEnv");
        left = store.select(jobName, timestampP, QueryOnMetadata.builder(map).build());
        assert left.size() == 8;
        LinkedHashMap<String, String> map1 = new LinkedHashMap<String, String>(1);
        map1.put("profile", "MyAdmin_DevelopmentEnv");
        right = store.select(jobName, timestampD, QueryOnMetadata.builder(map1).build());
        assert right.size() == 8;
    }

    @Test
    public void test_smoke() throws MaterialstoreException {
        Inspector inspector = Inspector.newInstance(store);
        MProductGroup reducedMPG = MProductGroup.builder(left, right).ignoreKeys("profile", "URL.host").build();
        MProductGroup processedMPG = inspector.process(reducedMPG);
        Double criteria = 0.0d;
        int warnings = processedMPG.countWarnings(criteria);
        // compile the report
        Path reportFile = inspector.report(processedMPG, criteria,
                jobName.toString() + "-index.html");
        assert processedMPG.size() == 8;
    }
}
