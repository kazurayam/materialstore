package com.kazurayam.materialstore.core.filesystem;

import com.kazurayam.materialstore.core.util.TestFixtureUtil;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

public class JobberTest {

    private static final Path outputDir = Paths.get(".").resolve("build/tmp/testOutput").resolve(JobberTest.class.getName());
    private static final Path imagesDir = Paths.get(".").resolve("src/test/fixtures/sample_images");
    private static final Path resultsDir = Paths.get(".").resolve("src/test/fixtures/sample_results");
    private Store store;

    @BeforeAll
    public static void beforeAll() throws IOException {
        if (Files.exists(outputDir)) {
            FileUtils.deleteDirectory(outputDir.toFile());
        }
        Files.createDirectories(outputDir);
    }

    @BeforeEach
    public void beforeEach() {
        Path root = outputDir.resolve("store");
        store = Stores.newInstance(root);
    }

    @Test
    public void test_constructor() throws MaterialstoreException {
        JobName jobName = new JobName("test_constructor");
        TestFixtureUtil.setupFixture(store, jobName);
        Jobber jobber = store.getJobber(jobName, new JobTimestamp("20210713_093357"));
        Assertions.assertNotNull(jobber);
        // When constructed, the Jobber object should deserialize the index file from Disk
        Assertions.assertEquals(3, jobber.size());
    }

    @Test
    public void test_read_by_ID() throws MaterialstoreException {
        JobName jobName = new JobName("test_read_by_ID");
        TestFixtureUtil.setupFixture(store, jobName);
        //
        JobTimestamp jobTimestamp = new JobTimestamp("20210713_093357");
        Jobber jobber = new Jobber(store, jobName, jobTimestamp);
        ID id = new ID("12a1a5ee4d0ee278ef4998c3f4ebd4951e6d2490");
        byte[] data = jobber.read(id, FileType.PNG);
        Assertions.assertNotNull(data);
    }

    @Test
    public void test_read_by_Material() throws MaterialstoreException {
        JobName jobName = new JobName("test_read_by_Material");
        TestFixtureUtil.setupFixture(store, jobName);
        JobTimestamp jobTimestamp = new JobTimestamp("20210713_093357");
        Jobber jobber = new Jobber(store, jobName, jobTimestamp);
        Material material = jobber.selectMaterial(new ID("12a1a5ee4d0ee278ef4998c3f4ebd4951e6d2490"));
        assert material != null;
        byte[] data = jobber.read(material);
        Assertions.assertNotNull(data);
    }

    /**
     * selecting a single Material object by ID
     */
    @Test
    public void test_selectMaterial_byID() throws MaterialstoreException {
        JobName jobName = new JobName("test_selectMaterial_byID");
        TestFixtureUtil.setupFixture(store, jobName);
        JobTimestamp jobTimestamp = new JobTimestamp("20210713_093357");
        Jobber jobber = new Jobber(store, jobName, jobTimestamp);
        Material material = jobber.selectMaterial(new ID("12a1a5ee4d0ee278ef4998c3f4ebd4951e6d2490"));
        Assertions.assertNotNull(material);
    }

    @Test
    public void test_selectMaterials_with_FileType() throws MaterialstoreException, IOException {
        JobName jobName = new JobName("test_selectMaterials_with_FileType");
        JobTimestamp jobTimestamp = JobTimestamp.now();
        Jobber jobber = store.getJobber(jobName, jobTimestamp);
        Metadata metadata = Metadata.builder(new URL("http://demoaut-mimic.katalon.com/")).put("environment", "DevelopmentEnv").build();
        BufferedImage image = ImageIO.read(imagesDir.resolve("20210623_225337.development.png").toFile());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, FileType.PNG.getExtension(), baos);
        byte[] data = baos.toByteArray();
        Material material = jobber.write(data, FileType.PNG, metadata);
        //
        QueryOnMetadata pattern = QueryOnMetadata.builder().put("environment", Pattern.compile(".*")).build();
        MaterialList materialList = jobber.selectMaterials(FileType.PNG, pattern);
        Assertions.assertNotNull(materialList);
        Assertions.assertEquals(1, materialList.size());
    }

    @Test
    public void test_selectMaterials_without_FileType() throws MaterialstoreException {
        JobName jobName = new JobName("test_selectMaterials_without_FileType");
        TestFixtureUtil.setupFixture(store, jobName);
        //
        JobTimestamp jobTimestamp = new JobTimestamp("20210715_145922");
        Jobber jobber = new Jobber(store, jobName, jobTimestamp);
        QueryOnMetadata pattern = QueryOnMetadata.builder().put("environment", "DevelopmentEnv").put("URL.host", Pattern.compile(".*")).build();
        // select without FileType
        MaterialList materialList = jobber.selectMaterials(pattern);
        Assertions.assertNotNull(materialList);
        Assertions.assertTrue(materialList.size() > 0);
    }

    @Test
    public void test_selectMaterial_with_QueryOnMetadataANY() throws MaterialstoreException {
        JobName jobName = new JobName("test_selectMaterials_without_FileType");
        TestFixtureUtil.setupFixture(store, jobName);
        //
        JobTimestamp jobTimestamp = new JobTimestamp("20210715_145922");
        Jobber jobber = new Jobber(store, jobName, jobTimestamp);
        // select with QueryOnMetadata.ANY, which means all Materials in the job directory
        MaterialList materialList = jobber.selectMaterials(QueryOnMetadata.ANY);
        Assertions.assertNotNull(materialList);
        Assertions.assertEquals(6, materialList.size());
    }

    @Test
    public void test_write() throws MaterialstoreException, IOException {
        JobName jobName = new JobName("test_write");
        JobTimestamp jobTimestamp = JobTimestamp.now();
        Jobber jobber = store.getJobber(jobName, jobTimestamp);
        LinkedHashMap<String, String> map = new LinkedHashMap<>(2);
        map.put("environment", "DevelopmentEnv");
        map.put("URL", "http://demoaut-mimic.katalon.com/");
        Metadata metadata = Metadata.builder(map).build();
        BufferedImage image = ImageIO.read(imagesDir.resolve("20210623_225337.development.png").toFile());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, FileType.PNG.getExtension(), baos);
        byte[] data = baos.toByteArray();
        Material material = jobber.write(data, FileType.PNG, metadata);
        //
        Assertions.assertNotNull(material);
    }

    @Test
    public void test_write_2_files_of_the_same_bytes_with_different_metadata() throws MaterialstoreException {
        JobName jobName = new JobName("test_write_2_files_of_the_same_bytes_with_different_metadata");
        JobTimestamp jobTimestamp = JobTimestamp.now();
        Jobber jobber = store.getJobber(jobName, jobTimestamp);
        byte[] data = "foo".getBytes();
        //
        LinkedHashMap<String, String> map = new LinkedHashMap<>(2);
        map.put("environment", "ProductionEnv");
        map.put("URL", "http://example.com");
        Metadata metadata1 = Metadata.builder(map).build();
        Material material1 = jobber.write(data, FileType.TXT, metadata1);
        assert material1 != null;
        //
        LinkedHashMap<String, String> map1 = new LinkedHashMap<>(2);
        map1.put("environment", "DevelopmentEnv");
        map1.put("URL", "http://example.com");
        Metadata metadata2 = Metadata.builder(map1).build();
        Material material2 = jobber.write(data, FileType.TXT, metadata2);
        assert material2 != null;
    }

    @Test
    public void test_write_duplicating_metadata_TERMINATE() throws MaterialstoreException {
        JobName jobName = new JobName("test_write_duplicating_metadata_Terminate");
        JobTimestamp jobTimestamp = JobTimestamp.now();
        final Jobber jobber = store.getJobber(jobName, jobTimestamp);
        LinkedHashMap<String, String> map = new LinkedHashMap<>(2);
        map.put("environment", "SomeEnv");
        map.put("URL", "http://example.com");
        final Metadata metadata = Metadata.builder(map).build();
        final byte[] data = "foo".getBytes();
        // write one file
        jobber.write(data, FileType.TXT, metadata);
        // try to write one more file with the same metadata with Jobber.DuplicationHandling as default(Terminate)
        MaterialstoreException thrown = Assertions.assertThrows(MaterialstoreException.class, () -> {
            jobber.write(data, FileType.TXT, metadata);
        });
        // try to write one more file with the same metadata with Jobber.DuplicationHandling.Terminate
        MaterialstoreException thrown2 = Assertions.assertThrows(MaterialstoreException.class, () -> {
            jobber.write(data, FileType.TXT, metadata);
        });
        Assertions.assertTrue(thrown.getMessage().contains("is already there in the index"));
    }

    @Test
    public void test_write_duplicating_metadata_CONTINUE() throws MaterialstoreException {
        JobName jobName = new JobName("test_write_duplicating_metadata_SkipOne");
        JobTimestamp jobTimestamp = JobTimestamp.now();
        Jobber jobber = store.getJobber(jobName, jobTimestamp);
        LinkedHashMap<String, String> map = new LinkedHashMap<>(2);
        map.put("environment", "SomeEnv");
        map.put("URL", "http://example.com");
        Metadata metadata = Metadata.builder(map).build();
        byte[] data = "foo".getBytes();
        // write one file
        jobber.write(data, FileType.TXT, metadata);

        // Now I will try to write one more file with the same metadata
        // with Jobber.DuplicationHandling.SkipOne
        jobber.write(data, FileType.TXT, metadata, Jobber.DuplicationHandling.CONTINUE);
        // should continue processing
        // then there should be only one file in the objects directory
        Assertions.assertEquals(1, jobber.size());
    }
}
