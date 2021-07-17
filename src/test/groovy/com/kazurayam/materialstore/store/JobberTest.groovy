package com.kazurayam.materialstore.store


import com.kazurayam.materialstore.MaterialstoreException
import com.kazurayam.materialstore.TestFixtureUtil
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.jupiter.api.Assertions.*

class JobberTest {

    private static Path outputDir =
            Paths.get(".").resolve("build/tmp/testOutput")
                    .resolve(JobberTest.class.getName())

    private static Path imagesDir =
            Paths.get(".").resolve("src/test/resources/fixture/sample_images")

    private static Path resultsDir =
            Paths.get(".").resolve("src/test/resources/fixture/sample_results")

    @BeforeAll
    static void beforeAll() {
        if (Files.exists(outputDir)) {
            FileUtils.deleteDirectory(outputDir.toFile())
        }
        Files.createDirectories(outputDir)
    }

    @BeforeEach
    void beforeEach() {
    }


    @Test
    void test_constructor() {
        Path root = outputDir.resolve("Materials")
        Store store = new StoreImpl(root)
        JobName jobName = new JobName("test_constructor")
        TestFixtureUtil.setupFixture(store, jobName)
        //
        Jobber jobber = store.getJobber(jobName,
                new JobTimestamp("20210713_093357"))
        assertNotNull(jobber)
        // When constructed, the Jobber object should deserialize the index file from Disk
        assertEquals(3, jobber.size())
    }

    @Test
    void test_write() {
        Path root = outputDir.resolve("Materials")
        StoreImpl repos = new StoreImpl(root)
        JobName jobName = new JobName("test_commit")
        JobTimestamp jobTimestamp = JobTimestamp.now()
        Jobber jobber = repos.getJobber(jobName, jobTimestamp)
        Metadata metadata = new Metadata(["profile": "DevelopmentEnv", "URL": "http://demoaut-mimic.katalon.com/"])
        BufferedImage image =  ImageIO.read(imagesDir.resolve("20210623_225337.development.png").toFile())
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, FileType.PNG.getExtension(), baos);
        byte[] data = baos.toByteArray()
        Material material = jobber.write(data, FileType.PNG, metadata)
        //
        assertNotNull(material)
    }

    @Test
    void test_write_duplicating() {
        Path root = outputDir.resolve("Materials")
        StoreImpl repos = new StoreImpl(root)
        JobName jobName = new JobName("test_commit_duplicating")
        JobTimestamp jobTimestamp = JobTimestamp.now()
        Jobber jobber = repos.getJobber(jobName, jobTimestamp)
        Metadata metadata = new Metadata(["profile":"SomeEnv", "URL":"http://example.com"])
        byte[] data = "foo".getBytes()
        jobber.write(data, FileType.TXT, metadata)
        MaterialstoreException thrown = assertThrows(MaterialstoreException.class, { ->
            jobber.write(data, FileType.TXT, metadata)
        })
        assertTrue(thrown.getMessage().contains("MObject is already in the Store"))
    }

    @Test
    void test_read_by_ID() {
        Path root = outputDir.resolve("Materials")
        Store store = new StoreImpl(root)
        JobName jobName = new JobName("test_read_by_ID")
        TestFixtureUtil.setupFixture(store, jobName)
        //
        JobTimestamp jobTimestamp = new JobTimestamp("20210713_093357")
        Jobber jobber = new Jobber(root, jobName, jobTimestamp)
        ID id = new ID("12a1a5ee4d0ee278ef4998c3f4ebd4951e6d2490")
        byte[] data = jobber.read(id, FileType.PNG)
        assertNotNull(data)
    }

    @Test
    void test_read_by_Material() {
        Path root = outputDir.resolve("Materials")
        Store store = new StoreImpl(root)
        JobName jobName = new JobName("test_read_by_Material")
        TestFixtureUtil.setupFixture(store, jobName)
        //
        JobTimestamp jobTimestamp = new JobTimestamp("20210713_093357")
        Jobber jobber = new Jobber(root, jobName, jobTimestamp)
        Material material = jobber.selectMaterial(
                new ID("12a1a5ee4d0ee278ef4998c3f4ebd4951e6d2490"))
        byte[] data = jobber.read(material)
        assertNotNull(data)
    }

    /**
     * selecting a single Material object by ID
     */
    @Test
    void test_selectMaterial() {
        Path root = outputDir.resolve("Materials")
        Store store = new StoreImpl(root)
        JobName jobName = new JobName("test_selectMaterial")
        TestFixtureUtil.setupFixture(store, jobName)
        //
        JobTimestamp jobTimestamp = new JobTimestamp("20210713_093357")
        Jobber jobber = new Jobber(root, jobName, jobTimestamp)
        Material material = jobber.selectMaterial(
                new ID("12a1a5ee4d0ee278ef4998c3f4ebd4951e6d2490"))
        assertNotNull(material)
    }

    @Test
    void test_selectMaterials() {
        Path root = outputDir.resolve("Materials")
        StoreImpl repos = new StoreImpl(root)
        Jobber jobber = repos.getJobber(new JobName("test_select"), JobTimestamp.now())
        Metadata metadata = new Metadata(["profile": "DevelopmentEnv", "URL": "http://demoaut-mimic.katalon.com/"])
        BufferedImage image =  ImageIO.read(imagesDir.resolve("20210623_225337.development.png").toFile())
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, FileType.PNG.getExtension(), baos);
        byte[] data = baos.toByteArray()
        Material material = jobber.write(data, FileType.PNG, metadata)
        //
        MetadataPattern pattern = new MetadataPattern([ "profile": "*", "URL": "*"])
        List<Material> materials = jobber.selectMaterials(FileType.PNG, pattern)
        assertNotNull(materials)
        assertEquals(1, materials.size())
    }
}
