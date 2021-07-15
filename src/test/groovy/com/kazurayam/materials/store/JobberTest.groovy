package com.kazurayam.materials.store


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

    @BeforeAll
    static void beforeAll() {
        Files.createDirectories(outputDir)
    }

    @BeforeEach
    void beforeEach() {
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
        jobber.commit(data, FileType.PNG, metadata)
        //
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
}
