package issues;

import com.kazurayam.materialstore.TestOutputOrganizerFactory;
import com.kazurayam.materialstore.base.reduce.MaterialProductGroup;
import com.kazurayam.materialstore.base.reduce.Reducer;
import com.kazurayam.materialstore.core.FileType;
import com.kazurayam.materialstore.core.JobName;
import com.kazurayam.materialstore.core.JobNameNotFoundException;
import com.kazurayam.materialstore.core.JobTimestamp;
import com.kazurayam.materialstore.core.MaterialList;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.QueryOnMetadata;
import com.kazurayam.materialstore.core.Store;
import com.kazurayam.materialstore.core.Stores;
import com.kazurayam.unittest.TestOutputOrganizer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Original problem was reported at
 * https://github.com/kazurayam/VisualInspectionOfExcelAndPDF/issues/3
 *
 * StoreImpl#reflect(MaterialList) does not work as expected.
 */
public class Issue217Test {

    private static final TestOutputOrganizer too =
            TestOutputOrganizerFactory.create(Issue217Test.class);
    static final Path fixtureDir =
            too.getProjectDir().resolve("src/test/fixtures/issue#217");
    private static Store store;
    private static final JobName jobName = new JobName("NISA");

    static {
        System.setProperty("org.slf4j.simpleLogger.log.com.kazurayam.materialstore.filesystem.StoreImpl", "DEBUG");
        System.setProperty("org.slf4j.simpleLogger.log.com.kazurayam.materialstore.reduce.MProductGroupBuilder", "DEBUG");
    }

    @BeforeAll
    public static void beforeAll() throws IOException {
        too.cleanClassOutputDirectory();
        Path outputDir = too.getClassOutputDirectory();
        too.copyDir(fixtureDir, outputDir);
        Path storePath = outputDir.resolve("store");
        store = Stores.newInstance(storePath);
    }

    @Test
    public void test_MaterialProductGroupBuilder_chronos() throws MaterialstoreException, JobNameNotFoundException {
        MaterialList currentMaterialList =
                store.select(jobName,
                        new JobTimestamp("20220406_134203"),
                        FileType.CSV,
                        QueryOnMetadata.ANY);
        BiFunction<MaterialList, MaterialList, MaterialProductGroup> func =
                (left, right) -> MaterialProductGroup.builder(left, right).build();

        MaterialProductGroup reducedMPG = Reducer.chronos(store, currentMaterialList, func);
        assertNotNull(reducedMPG);
        assertEquals(15, reducedMPG.size());
    }
}
