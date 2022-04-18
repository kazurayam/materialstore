package issues;

import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.FileType;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.filesystem.QueryOnMetadata;
import com.kazurayam.materialstore.filesystem.Store;

import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.Stores;
import com.kazurayam.materialstore.reduce.MProductGroup;
import com.kazurayam.materialstore.reduce.MProductGroupBuilder;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    static final Path fixtureDir = Paths.get(".")
            .resolve("src/test/fixture/issue#217");
    static final Path outputDir = Paths.get(".")
            .resolve("build/tmp/testOutput")
            .resolve(Issue217Test.class.getName());

    private static Store store;
    private static final JobName jobName = new JobName("NISA");

    static {
        System.setProperty("org.slf4j.simpleLogger.log.com.kazurayam.materialstore.filesystem.StoreImpl", "DEBUG");
        System.setProperty("org.slf4j.simpleLogger.log.com.kazurayam.materialstore.reduce.MProductGroupBuilder", "DEBUG");
    }

    @BeforeAll
    public static void beforeAll() throws IOException {
        if (Files.exists(outputDir)) {
            FileUtils.deleteDirectory(outputDir.toFile());
        }
        Files.createDirectories(outputDir);
        FileUtils.copyDirectory(fixtureDir.toFile(), outputDir.toFile());
        Path storePath = outputDir.resolve("store");
        store = Stores.newInstance(storePath);
    }

    @Test
    public void test_MProductGroupBuilder_chronos() throws MaterialstoreException {
        MaterialList currentMaterialList =
                store.select(jobName,
                        new JobTimestamp("20220406_134203"),
                        FileType.CSV,
                        QueryOnMetadata.ANY);
        BiFunction<MaterialList, MaterialList, MProductGroup> func =
                (left, right) -> MProductGroup.builder(left, right).build();

        MProductGroup prepared = MProductGroupBuilder.chronos(store, currentMaterialList, func);
        assertNotNull(prepared);
        assertEquals(15, prepared.size());
    }



}
