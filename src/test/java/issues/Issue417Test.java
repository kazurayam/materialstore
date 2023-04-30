package issues;

import com.kazurayam.materialstore.base.reduce.MaterialProductGroup;
import com.kazurayam.materialstore.base.reduce.differ.ImageDifferToPNG;
import com.kazurayam.materialstore.base.reduce.zipper.MaterialProduct;
import com.kazurayam.materialstore.core.FileType;
import com.kazurayam.materialstore.core.IFileType;
import com.kazurayam.materialstore.core.JobName;
import com.kazurayam.materialstore.core.JobTimestamp;
import com.kazurayam.materialstore.core.MaterialList;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.Metadata;
import com.kazurayam.materialstore.core.QueryOnMetadata;
import com.kazurayam.materialstore.core.Store;
import com.kazurayam.materialstore.core.Stores;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Reproducing the issue https://github.com/kazurayam/materialstore/issues/417
 *
 * com.kazurayam.materialstore.base.reduce.differ.ImageDifferToPNG.stuffDiff(ImageDifferToPNG.java:41　
 * caused OutOfMemoryError
 */
public class Issue417Test {

    private static Path fixtureDir =
            Paths.get(".").resolve("src/test/fixtures/issue417");

    private static Path workDir =
            Paths.get(".").resolve("build/tmp/testOutput")
                    .resolve(Issue417Test.class.getSimpleName());
    private static Store store;
    private static JobName jobName = new JobName("NISA_Chronos");
    private static JobTimestamp jobTimestamp;

    @BeforeAll
    public static void beforeAll() throws MaterialstoreException {
        Path rootDir = workDir.resolve("store");
        store = Stores.newInstance(rootDir);
        jobTimestamp = JobTimestamp.now();
        Metadata metadata = Metadata.builder().build();
        Path pngFile = fixtureDir.resolve("3a98c4ba471f11462d06a4c94ef4daa4010a466a.png");
        store.write(jobName, jobTimestamp, FileType.PNG, metadata, pngFile);
    }

    @Test
    public void test_reproduce() throws MaterialstoreException {
        MaterialList left = store.select(jobName, jobTimestamp,
                FileType.PNG, QueryOnMetadata.ANY);
        MaterialList right = store.select(jobName, JobTimestamp.laterThan(jobTimestamp),
                FileType.PNG, QueryOnMetadata.ANY);
        MaterialProductGroup mpg = MaterialProductGroup.builder(left, right).build();
        MaterialProduct stuffed = new ImageDifferToPNG(store).stuffDiff(mpg.get(0));
        assertNotNull(stuffed);
        // OutOfMemoryError will occur
    }


}
