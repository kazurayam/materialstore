package my.sample;

import com.kazurayam.materialstore.core.filesystem.JobName;
import com.kazurayam.materialstore.core.filesystem.JobTimestamp;
import com.kazurayam.materialstore.core.filesystem.Material;
import com.kazurayam.materialstore.core.filesystem.MaterialList;
import com.kazurayam.materialstore.core.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.core.filesystem.QueryOnMetadata;
import com.kazurayam.materialstore.core.filesystem.Store;
import com.kazurayam.materialstore.core.filesystem.Stores;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

public class T05SelectMaterialListTest {
    private Store store;
    @BeforeEach
    public void beforeEach() throws IOException {
        Path testClassOutputDir = TestHelper.createTestClassOutputDir(this);
        store = Stores.newInstance(testClassOutputDir.resolve("store"));
    }

    @Test
    public void test05_select_list_of_material()
            throws MaterialstoreException {
        JobName jobName =
                new JobName("test04_select_lest_of_materials");
        JobTimestamp jobTimestamp = JobTimestamp.now();
        SharedMethods.write3images(store, jobName, jobTimestamp);
        //
        MaterialList materialList =
                store.select(jobName, jobTimestamp,
                        QueryOnMetadata.ANY);              // (18)
        //
        for (Material material : materialList) {           // (19)
            System.out.printf("%s %s%n",
                    material.getFileType().getExtension(),
                    material.getMetadata().getMetadataIdentification());
        }
    }
}
