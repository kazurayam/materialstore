package my.sample;

import com.kazurayam.materialstore.core.filesystem.FileType;
import com.kazurayam.materialstore.core.filesystem.JobName;
import com.kazurayam.materialstore.core.filesystem.JobTimestamp;
import com.kazurayam.materialstore.core.filesystem.Material;
import com.kazurayam.materialstore.core.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.core.filesystem.Metadata;
import com.kazurayam.materialstore.core.filesystem.Store;
import com.kazurayam.materialstore.core.filesystem.Stores;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/*
 * This code demonstrate how to save a text string into an instance of
 * "materialstore" backed with a directory on the local OS file system.
 */
public class T1HelloMaterialstoreTest {

    // central abstraction of Material storage
    private Store store;

    private Logger logger = LoggerFactory.getLogger(T1HelloMaterialstoreTest.class);

    @BeforeEach
    public void beforeEach() {
        // create a base directory
        Path dir = createTestClassOutputDir(this);   // (1)
        // create a directory named "store"
        Path storeDir = dir.resolve("store");   // (2)
        // instantiate a Store object
        store = Stores.newInstance(storeDir);        // (3)
    }

    @Test
    public void test01_hello_materialstore() throws MaterialstoreException {
        JobName jobName =
                new JobName("test01_hello_materialstore");       // (4)
        JobTimestamp jobTimestamp = JobTimestamp.now();          // (5)
        String text = "Hello, materialstore!";
        Material material = store.write(jobName, jobTimestamp,   // (6)
                FileType.TXT,                            // (7)
                Metadata.NULL_OBJECT,                    // (8)
                text);                                   // (9)
        logger.info(String.format("wrote a text '%s'", text));
        assertNotNull(material);
    }

    //-----------------------------------------------------------------

    Path createTestClassOutputDir(Object testClass) {
        Path output = getTestOutputDir()
                .resolve(testClass.getClass().getName());
        try {
            if (!Files.exists(output)) {
                Files.createDirectories(output);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return output;
    }

    Path getTestOutputDir() {
        return Paths.get(System.getProperty("user.dir"))
                .resolve("build/tmp/testOutput");
    }
}

