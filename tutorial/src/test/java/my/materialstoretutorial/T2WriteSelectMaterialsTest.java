package my.materialstoretutorial;

import com.kazurayam.materialstore.core.filesystem.FileType;
import com.kazurayam.materialstore.core.filesystem.JobName;
import com.kazurayam.materialstore.core.filesystem.JobTimestamp;
import com.kazurayam.materialstore.core.filesystem.Material;
import com.kazurayam.materialstore.core.filesystem.MaterialList;
import com.kazurayam.materialstore.core.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.core.filesystem.Metadata;
import com.kazurayam.materialstore.core.filesystem.QueryOnMetadata;
import com.kazurayam.materialstore.core.filesystem.Store;
import com.kazurayam.materialstore.core.filesystem.Stores;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class T2WriteSelectMaterialsTest {

    private Logger logger = LoggerFactory.getLogger(T1HelloMaterialstoreTest.class);
    private Path testClassOutputDir;
    private Store store;

    @BeforeEach
    public void beforeEach() throws IOException {
        testClassOutputDir = TestHelper.createTestClassOutputDir(this);
        store = Stores.newInstance(testClassOutputDir.resolve("store"));
    }

    @Test
    public void test02_write_text_with_metadata() throws MaterialstoreException {
        JobName jobName = new JobName("test02_write_text_with_metadata");
        JobTimestamp jobTimestamp = JobTimestamp.now();
        store.write(jobName, jobTimestamp, FileType.TXT,
                Metadata.builder().put("step", "01").build(),
                "I have some metadata!");
    }

    private void writeTripleTexts(Store store,
                                  JobName jobName,
                                  JobTimestamp jobTimestamp)
            throws MaterialstoreException {
        store.write(jobName, jobTimestamp, FileType.TXT,
                Metadata.builder().put("step", "01").build(),
                "I woke up this morning early");
        store.write(jobName, jobTimestamp, FileType.TXT,
                Metadata.builder().put("step", "02").build(),
                "I drank a pint at lunch");
        store.write(jobName, jobTimestamp, FileType.TXT,
                Metadata.builder().put("step", "03").build(),
                "I jogged around the park at the sunset");
    }

    @Test
    public void test03_count_materials() throws MaterialstoreException {
        JobName jobName = new JobName("test03_count_materials");
        JobTimestamp jobTimestamp = JobTimestamp.now();
        writeTripleTexts(store, jobName, jobTimestamp);
        //
        MaterialList allMaterialList =
                store.select(jobName, jobTimestamp, QueryOnMetadata.ANY);
        assertEquals(3, allMaterialList.size());
    }

    @Test
    public void test04_select_all_material() throws MaterialstoreException {
        JobName jobName = new JobName("test04_select_all_materials");
        JobTimestamp jobTimestamp = JobTimestamp.now();
        writeTripleTexts(store, jobName, jobTimestamp);
        //
        MaterialList allMaterialList =
                store.select(jobName, jobTimestamp, QueryOnMetadata.ANY);
        int lineCount = 0;
        for (Material material : allMaterialList) {
            lineCount += 1;
            if (material.getFileType().equals(FileType.TXT)) {
                Path mt = material.toPath(store);
                try {
                    List<String> lines = Files.readAllLines(mt);
                    for (String line : lines) {
                        logger.info(String.format("[%s] %d '%s'",
                                jobName, lineCount, line));
                    }
                } catch (IOException e) {
                    throw new MaterialstoreException(e);
                }
            } else {
                fail(String.format("material.getFile() is %s, which is unexpected",
                        material.getFileType().toString()));
            }
        }
    }

    @Test
    public void test05_select_a_material() throws MaterialstoreException {
        JobName jobName = new JobName("test05_select_a_material");
        JobTimestamp jobTimestamp = JobTimestamp.now();
        writeTripleTexts(store, jobName, jobTimestamp);
        // select a Material out of 3
        Material material = store.selectSingle(jobName, jobTimestamp,
                QueryOnMetadata.builder().put("step", "02").build());
        assertNotNull(material);
        try {
            List<String> lines = Files.readAllLines(material.toPath(store));
            assertEquals("I drank a pint at lunch", lines.get(0));
        } catch (IOException e) {
            throw new MaterialstoreException(e);
        }
    }

}
