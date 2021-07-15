package com.kazurayam.materials.store

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

import com.kazurayam.materials.MaterialsException

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
        // make sure the Job directory to be empty
        FileUtils.deleteDirectory(root.resolve(jobName.toString()).toFile())
        // stuff the Job directory with a fixture
        Path jobNameDir = root.resolve(jobName.toString())
        FileUtils.copyDirectory(resultsDir.toFile(), jobNameDir.toFile())
        //
        Jobber jobber = store.getJobber(jobName,
                new JobTimestamp("20210713_093357"))
        assertNotNull(jobber)
        // When constructed, the Jobber object should deserialize the index file from Disk
        assertEquals(3, jobber.size())
    }

    @Test
    void test_commit() {
        Path root = outputDir.resolve("Materials")
        StoreImpl repos = new StoreImpl(root)
        Jobber jobber = repos.getJobber(new JobName("test_commit"), JobTimestamp.now())
        Metadata metadata = new Metadata("DevelopmentEnv", "http://demoaut-mimic.katalon.com/")
        BufferedImage image =  ImageIO.read(imagesDir.resolve("20210623_225337.development.png").toFile())
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, FileType.PNG.getExtension(), baos);
        byte[] data = baos.toByteArray()
        Material material = jobber.commit(data, FileType.PNG, metadata)
        //
        assertNotNull(material)
    }

    @Test
    void test_commit_duplicating() {
        Path root = outputDir.resolve("Materials")
        StoreImpl repos = new StoreImpl(root)
        Jobber jobber = repos.getJobber(new JobName("test_commit_duplicating"), JobTimestamp.now())
        Metadata metadata = new Metadata("SomeEnv", "http://example.com")
        byte[] data = "foo".getBytes()
        jobber.commit(data, FileType.TXT, metadata)
        MaterialsException thrown = assertThrows(MaterialsException.class, { ->
            jobber.commit(data, FileType.TXT, metadata)
        })
        assertTrue(thrown.getMessage().contains("MObject is already in the Store"))
    }

    @Test
    void test_select() {
        Path root = outputDir.resolve("Materials")
        StoreImpl repos = new StoreImpl(root)
        Jobber jobber = repos.getJobber(new JobName("test_select"), JobTimestamp.now())
        Metadata metadata = new Metadata("DevelopmentEnv", "http://demoaut-mimic.katalon.com/")
        BufferedImage image =  ImageIO.read(imagesDir.resolve("20210623_225337.development.png").toFile())
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, FileType.PNG.getExtension(), baos);
        byte[] data = baos.toByteArray()
        Material material = jobber.commit(data, FileType.PNG, metadata)
        //
        MetadataPattern pattern = new MetadataPattern("*", "*")
        List<Material> materials = jobber.select(FileType.PNG, pattern)
        assertNotNull(materials)
        assertEquals(1, materials.size())
    }
}
