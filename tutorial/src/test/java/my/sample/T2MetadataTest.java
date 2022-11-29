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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.MalformedInputException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

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
        URL url = createURL("https://kazurayam.github.io/materialstore/images/tutorial/03_apple.png");
        byte[] bytes = downloadUrl(url);
        store.write(jobName, jobTimestamp, FileType.PNG,
                Metadata.builder(url).put("step", "01").build(), bytes);
    }

    URL createURL(String urlString) throws MaterialstoreException {
        try {
            return new URL(urlString);
        } catch (MalformedURLException e) {
            throw new MaterialstoreException(e);
        }
    }

    byte[] downloadUrl(URL toDownload) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            byte[] chunk = new byte[4096];
            int bytesRead;
            InputStream stream = toDownload.openStream();
            while ((bytesRead = stream.read(chunk)) > 0) {
                outputStream.write(chunk, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return outputStream.toByteArray();
    }


    @Test
    public void test03_write_multiple_images() throws MaterialstoreException {
        JobName jobName = new JobName("test03_write_multiple_images");
        JobTimestamp jobTimestamp = JobTimestamp.now();
        write3images(store, jobName, jobTimestamp);
        MaterialList allMaterialList =
                store.select(jobName, jobTimestamp, QueryOnMetadata.ANY);
        assertEquals(3, allMaterialList.size());
    }
    void write3images(Store store, JobName jobName, JobTimestamp jobTimestamp)
            throws MaterialstoreException {
        URL url1 = createURL("https://kazurayam.github.io/materialstore/images/tutorial/03_apple.png");
        store.write(jobName, jobTimestamp, FileType.PNG,
                Metadata.builder(url1).put("step", "01").build(), downloadUrl(url1));;
        URL url2 = createURL("https://kazurayam.github.io/materialstore/images/tutorial/04_mikan.png");
        store.write(jobName, jobTimestamp, FileType.PNG,
                Metadata.builder(url2).put("step", "02").build(), downloadUrl(url2));;
        URL url3 = createURL("https://kazurayam.github.io/materialstore/images/tutorial/05_money.png");
        store.write(jobName, jobTimestamp, FileType.PNG,
                Metadata.builder(url3).put("step", "03").build(), downloadUrl(url3));;
    }

    @Test
    public void test04_select_all_material() throws MaterialstoreException {
        JobName jobName = new JobName("test04_select_all_materials");
        JobTimestamp jobTimestamp = JobTimestamp.now();
        write3images(store, jobName, jobTimestamp);
        //
        MaterialList allMaterialList =
                store.select(jobName, jobTimestamp, QueryOnMetadata.ANY);
        for (Material material : allMaterialList) {
            System.out.println(String.format("%s %s",
                    material.getFileType().getExtension(),
                    material.getMetadata().toString()));
        }
    }

    @Test
    public void test05_select_a_material() throws MaterialstoreException {
        JobName jobName = new JobName("test05_select_a_material");
        JobTimestamp jobTimestamp = JobTimestamp.now();
        write3images(store, jobName, jobTimestamp);
        //
        Material material = store.selectSingle(jobName, jobTimestamp,
                QueryOnMetadata.builder().put("step", "02").build());
        assertNotNull(material);
        assertEquals(FileType.PNG, material.getFileType());
        assertEquals("https",
                material.getMetadata().get("URL.protocol"));
        assertEquals("kazurayam.github.io",
                material.getMetadata().get("URL.host"));
        assertEquals("/materialstore/images/tutorial/04_mikan.png",
                material.getMetadata().get("URL.path"));
    }
}
