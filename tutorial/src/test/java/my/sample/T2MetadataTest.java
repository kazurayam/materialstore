package my.sample;

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

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class T2MetadataTest {

    private Store store;

    @BeforeEach
    public void beforeEach() throws IOException {
        Path testClassOutputDir = TestHelper.createTestClassOutputDir(this);
        store = Stores.newInstance(testClassOutputDir.resolve("store"));
    }

    @Test
    public void test02_write_image_with_metadata() throws MaterialstoreException {
        JobName jobName = new JobName("test02_write_image_with_metadata");
        JobTimestamp jobTimestamp = JobTimestamp.now();
        URL url = SharedMethods.createURL(                     // (10)
                "https://kazurayam.github.io/materialstore/images/tutorial/03_apple.png");
        byte[] bytes = SharedMethods.downloadUrl(url);         // (11)
        Material material =
                store.write(jobName, jobTimestamp,             // (12)
                        FileType.PNG,
                        Metadata.builder(url).put("step", "01") // (13)
                                .build(),
                        bytes);

        assertNotNull(material);
        System.out.println(material.getID() + " " +
                material.getDescription());                    // (14)
        assertEquals(FileType.PNG, material.getFileType());
        assertEquals("https",
                material.getMetadata().get("URL.protocol"));
        assertEquals("kazurayam.github.io",
                material.getMetadata().get("URL.host"));        // (15)
        assertEquals("/materialstore/images/tutorial/03_apple.png",
                material.getMetadata().get("URL.path"));
        assertEquals("01", material.getMetadata().get("step"));
    }



    @Test
    public void test03_write_multiple_images()
            throws MaterialstoreException {
        JobName jobName = new JobName("test03_write_multiple_images");
        JobTimestamp jobTimestamp = JobTimestamp.now();
        SharedMethods.write3images(store, jobName, jobTimestamp);                       // (16)
        MaterialList allMaterialList =
                store.select(jobName, jobTimestamp, QueryOnMetadata.ANY); // (17)
        assertEquals(3, allMaterialList.size());
    }


    @Test
    public void test04_select_list_of_material()
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

    @Test
    public void test05_select_a_single_material_with_query()
            throws MaterialstoreException {
        JobName jobName =
                new JobName("test05_select_a_single_material_with_query");
        JobTimestamp jobTimestamp = JobTimestamp.now();
        SharedMethods.write3images(store, jobName, jobTimestamp);
        //
        Material material = store.selectSingle(jobName, jobTimestamp,
                QueryOnMetadata.builder().put("step", "02").build()); // (20)
        assertNotNull(material);
    }
}
