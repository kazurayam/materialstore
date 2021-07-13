package com.kazurayam.taod

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

import static org.junit.jupiter.api.Assertions.*

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class JobResultTest {

    private static Path outputDir =
            Paths.get(".").resolve("build/tmp/testOutput")
                    .resolve(JobResultTest.class.getName())

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
        Path root = outputDir.resolve(".taod")
        Organizer repos = new Organizer(root)
        JobResult jobResult = repos.getJobResult(new JobName("test_commit"), JobTimestamp.now())
        Metadata metadata = new Metadata("DevelopEnv", "http://demoaut-mimic.katalon.com/")
        BufferedImage image =  ImageIO.read(imagesDir.resolve("20210623_225337.develop.png").toFile())
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, FileType.PNG.getExtension(), baos);
        byte[] data = baos.toByteArray()
        jobResult.commit(metadata, data, FileType.PNG)
        //

    }

    @Test
    void test_toString() {
        Path root = outputDir.resolve(".taod")
        Organizer organizer = new Organizer(root)
        JobResult jobResult = organizer.getJobResult(new JobName("test_toString"), JobTimestamp.now())
        //println job.toString()
        assertTrue(jobResult.toString().contains("\"jobName\":\"test_toString\""))
        assertTrue(jobResult.toString().contains("\"jobTimestamp\":"))
        assertTrue(jobResult.toString().contains("\"jobResultDir\":"))
    }
}
