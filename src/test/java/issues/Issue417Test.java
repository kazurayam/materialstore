package issues;

import com.kazurayam.materialstore.base.reduce.MaterialProductGroup;
import com.kazurayam.materialstore.base.reduce.differ.ImageDifferToPNG;
import com.kazurayam.materialstore.base.reduce.zipper.MaterialProduct;
import com.kazurayam.materialstore.core.FileType;
import com.kazurayam.materialstore.core.IFileType;
import com.kazurayam.materialstore.core.JobName;
import com.kazurayam.materialstore.core.JobTimestamp;
import com.kazurayam.materialstore.core.Material;
import com.kazurayam.materialstore.core.MaterialList;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.Metadata;
import com.kazurayam.materialstore.core.QueryOnMetadata;
import com.kazurayam.materialstore.core.Store;
import com.kazurayam.materialstore.core.Stores;
import com.kazurayam.materialstore.util.DeleteDir;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.qatools.ashot.comparison.ImageDiff;
import ru.yandex.qatools.ashot.comparison.ImageDiffer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
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

    private static Path projectDir = Paths.get(".");

    private static Path fixtureDir = projectDir.resolve("src/test/fixtures/issue#417");

    private static Path workDir = projectDir.resolve("build/tmp/testOutput")
                    .resolve(Issue417Test.class.getSimpleName());
    private static Store store;

    @BeforeAll
    public static void beforeAll() throws MaterialstoreException, IOException {
        DeleteDir.deleteDirectoryRecursively(workDir);
        Files.createDirectory(workDir);
        Path rootDir = workDir.resolve("store");
        store = Stores.newInstance(rootDir);
    }


    /**
     * When both of the left and right Material are given,
     * no Error will be raised
     */
    @Test
    public void test_normal_case() throws MaterialstoreException {
        // prepare the fixture files
        JobName jobName = new JobName("test_normal_case");
        Metadata metadata =
                Metadata.builder().put("description","fixture").build();
        Path pngFile = fixtureDir.resolve("3a98c4ba471f11462d06a4c94ef4daa4010a466a.png");
        JobTimestamp jtLeft = JobTimestamp.now();
        store.write(jobName, jtLeft, FileType.PNG, metadata, pngFile);
        JobTimestamp jtRight = JobTimestamp.laterThan(jtLeft);
        store.write(jobName, jtRight, FileType.PNG, metadata, pngFile);
        //
        MaterialList mlLeft = store.select(jobName, jtLeft,
                FileType.PNG, QueryOnMetadata.ANY);
        MaterialList mlRight = store.select(jobName, jtRight,
                FileType.PNG, QueryOnMetadata.ANY);
        MaterialProductGroup mpg = MaterialProductGroup.builder(mlLeft, mlRight).build();
        MaterialProduct stuffed = new ImageDifferToPNG(store).stuffDiff(mpg.get(0));
        assertNotNull(stuffed);
    }

    /**
     * When either of the left or right Material is a NULL_OBJECT,
     * ImageDifferToPNG.stuffDiff() throws the OutOfMemoryError
     */
    @Test
    public void test_reproduce_OutOfMemoryError() throws MaterialstoreException {
        // prepare the fixture files
        JobName jobName = new JobName("test_reproduce_OutOfMemoryError");
        JobTimestamp jtLeft = JobTimestamp.now();
        JobTimestamp jtRight = JobTimestamp.laterThan(jtLeft);
        Metadata metadata =
                Metadata.builder().put("description", "fixture").build();
        Path pngFile = fixtureDir.resolve("3a98c4ba471f11462d06a4c94ef4daa4010a466a.png");
        store.write(jobName, jtRight, FileType.PNG, metadata, pngFile);
        // invoke ImageDifferToPNG.stuffDiff() to reproduce the Error
        MaterialList left = store.select(jobName, jtLeft, FileType.PNG, QueryOnMetadata.ANY);
        MaterialList right = store.select(jobName, jtRight, FileType.PNG, QueryOnMetadata.ANY);
        MaterialProductGroup mpg = MaterialProductGroup.builder(left, right).build();
        MaterialProduct stuffed = new ImageDifferToPNG(store).stuffDiff(mpg.get(0));
        assertNotNull(stuffed);
        // OutOfMemoryError will occur
    }


    /**
     * Try to execute ru.yandex.qatools.ashot.comparison.ImageDiff#makeDiff(leftImage, rightImage)
     * with the leftImage being loaded from the main/resources/com/kazurayam/materialstore/core/NoMaterialFound.png
     * Interested in how much memory the method requires
     * as it possibly causes the OutOfMemoryError reported at
     * https://github.com/kazurayam/materialstore/issues/417 .
     */
    @Test
    public void test_AShot_imageDiff_how_much_memory_it_requires() throws MaterialstoreException, IOException {
        Path noMaterialFoundPNG = projectDir
                .resolve("src/main/resources/com/kazurayam/materialstore/core/NoMaterialFound.png");
        BufferedImage leftImage = ImageIO.read(noMaterialFoundPNG.toFile());
        Path fixturePNG = projectDir
                .resolve("src/test/fixtures/issue#417/3a98c4ba471f11462d06a4c94ef4daa4010a466a.png");
        BufferedImage rightImage = ImageIO.read(fixturePNG.toFile());
        ImageDiffer imgDiff = new ImageDiffer();
        ImageDiff imageDiff = imgDiff.makeDiff(leftImage, rightImage);
        // this causes OutOfMemoryError
        assertNotNull(imageDiff);
    }
}
