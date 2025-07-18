package issues;

import com.kazurayam.materialstore.zest.FixtureDirectory;
import com.kazurayam.materialstore.zest.TestOutputOrganizerFactory;
import com.kazurayam.materialstore.base.inspector.Inspector;
import com.kazurayam.materialstore.base.reduce.MaterialProductGroup;
import com.kazurayam.materialstore.base.reduce.differ.TextDifferToHTML;
import com.kazurayam.materialstore.base.reduce.zipper.MaterialProduct;
import com.kazurayam.materialstore.core.FileType;
import com.kazurayam.materialstore.core.JobName;
import com.kazurayam.materialstore.core.JobTimestamp;
import com.kazurayam.materialstore.core.Material;
import com.kazurayam.materialstore.core.MaterialList;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.Metadata;
import com.kazurayam.materialstore.core.Store;
import com.kazurayam.materialstore.core.Stores;
import com.kazurayam.materialstore.util.CopyDir;
import com.kazurayam.materialstore.util.DeleteDir;
import com.kazurayam.unittest.TestOutputOrganizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This test was developed initially to reproduce the problem raised at
 * - https://github.com/kazurayam/materialstore/issues/362
 * and to test any changes made to fix various issues around the issue362.
 * This test uses the files in the base/src/test/fixtures/issue#363 as fixture.
 * This fixture was generated by
 * - https://github.com/kazurayam/VisualInspectionInKatalonStudio_Reborn/blob/master/Scripts/main/MyAdmin/0_Main_Twins/Script1666270485532.groovy
 * with the materialstore-0.12.5-SNAPSHOT.
 *
 * This test performs the similar processing as
 * - main/MyAdmin/3_reduceTwins https://github.com/kazurayam/VisualInspectionInKatalonStudio_Reborn/blob/master/Scripts/main/MyAdmin/3_reduceTwins/Script1666270543170.groovy
 * which executes
 *
 * ```
 * MateriaProductGroup Inspector#reduceAndSort(MaterialProductGroup)
 * ```
 *
 * and verifies the outcome.
 */
@Disabled
public class Issue362Test {

    private static final TestOutputOrganizer too =
            TestOutputOrganizerFactory.create(Issue362Test.class);
    private Store store;
    private Path issue362fixtureDir;

    @BeforeEach
    public void beforeEach() throws IOException {
        store = Stores.newInstance(too.cleanClassOutputDirectory().resolve("store"));
        issue362fixtureDir = FixtureDirectory.getFixturesDirectory().resolve("issue#362");
    }

    @Test
    public void test_smoke() throws MaterialstoreException, IOException {
        JobName jobName = new JobName("test_smoke");
        JobTimestamp leftJobTimestamp = new JobTimestamp("20221119_085006");
        JobTimestamp rightJobTimestamp = new JobTimestamp("20221119_085016");
        // setup fixture
        deployFixture(jobName);
        // Prepare
        MaterialList leftMaterialList = store.select(jobName, leftJobTimestamp);
        MaterialList rightMaterialList = store.select(jobName, rightJobTimestamp);
        MaterialProductGroup mpg =
                MaterialProductGroup.builder(leftMaterialList, rightMaterialList)
                        .ignoreKeys("profile", "URL.host", "URL.port")
                        .labelLeft("ProductionEnv")
                        .labelRight("DevelopmentEnv")
                        .sort("step")
                        .build();
        Inspector inspector = Inspector.newInstance(store);

        // Action 1:
        MaterialProductGroup reduced = inspector.reduceAndSort(mpg);

        // verify the inspected MaterialProductGroup
        assertEquals(6,
                store.select(jobName, reduced.getJobTimestampOfReduceResult()).size()
        );

        // Action 2: compile HTML report
        Path report = inspector.report(reduced, 0.0);
        // verify the report
        assertTrue(Files.exists(report));
    }

    private void deployFixture(JobName jobName) throws IOException, MaterialstoreException {
        Path targetDir = store.getPathOf(jobName);
        // prepare the fixture files
        if (targetDir != null) {
            DeleteDir.deleteDirectoryRecursively(targetDir);
        }
        targetDir = store.getRoot().resolve(jobName.getJobName());
        Files.createDirectories(targetDir);
        Path sourceDir = issue362fixtureDir.resolve("store/MyAdmin");
        assert Files.exists(sourceDir);
        Files.walkFileTree(sourceDir, new CopyDir(sourceDir, targetDir));
    }

    @Test
    public void test_compare_logs() throws MaterialstoreException {
        JobName jobName = new JobName("test_compare_logs");
        JobTimestamp jobTimestamp = JobTimestamp.now();
        //
        Path fixtureDir = FixtureDirectory.getFixturesDirectory().resolve(
                "issue#362_DiffingMPGProcessor_debug_logs");
        Path jsonLeft = fixtureDir.resolve("before_engraving.json");
        Path jsonRight = fixtureDir.resolve("after_engraving.json");
        Material left = store.write(jobName, jobTimestamp, FileType.JSON,
                Metadata.builder().put("label", "before engraving").build(),
                jsonLeft);
        Material right = store.write(jobName, jobTimestamp, FileType.JSON,
                Metadata.builder().put("label", "after engraving").build(),
                jsonRight);
        //
        MaterialProduct mp = new MaterialProduct.Builder(left, right, jobName, jobTimestamp).build();
        TextDifferToHTML differ = new TextDifferToHTML(store);
        MaterialProduct stuffed = differ.generateDiff(mp);


    }
}
