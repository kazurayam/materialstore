package com.kazurayam.taod

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertTrue

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class OrganizerTest {

    private static Path outputDir =
            Paths.get(".").resolve("build/tmp/testOutput")
                    .resolve(OrganizerTest.class.getName())

    private static Path fixtureDir =
            Paths.get(".").resolve("src/test/resources/fixture")

    @BeforeAll
    static void beforeAll() {
        Files.createDirectories(outputDir)
    }

    @Test
    void test_getRoot() {
        Path root = outputDir.resolve(".taod")
        Organizer repos = new Organizer(root)
        assertTrue(Files.exists(repos.getRoot()), "${root} is not present")
        assertEquals(".taod", repos.getRoot().getFileName().toString())
    }

    @Test
    void test_getArtifacts() {
        Path root = outputDir.resolve(".taod")
        Organizer repos = new Organizer(root)
        JobName jobName = new JobName("test_getArtifacts")
        JobTimestamp jobTimestamp = JobTimestamp.now()
        Job job = repos.getJob(jobName, jobTimestamp)
        assertNotNull(job)
        assertEquals("test_getArtifacts", job.getJobName().toString())
    }

    @Test
    void test_write_path() {
        Path root = outputDir.resolve(".taod")
        Organizer organizer = new Organizer(root)
        JobName jobName = new JobName("test_write_path")
        JobTimestamp jobTimestamp = JobTimestamp.now()
        Metadata metadata = new Metadata("DevelopEnv", "http://demoaut-mimic.kazurayam.com/")
        Path input = fixtureDir.resolve("20210710_142631.develop.png")
        ID id = organizer.write(jobName, jobTimestamp, metadata, input, FileType.PNG)
        assertNotNull(id)
        assertTrue(ID.isValid(id.toString()))
    }

    @Test
    void test_write_BufferedImage() {
        Path root = outputDir.resolve(".taod")
        Organizer organizer = new Organizer(root)
        JobName jobName = new JobName("test_write_BufferedImage")
        JobTimestamp jobTimestamp = JobTimestamp.now()
        Metadata metadata = new Metadata("ProductEnv", "http://demoaut.katalon.com/")
        Path input = fixtureDir.resolve("20210710_142628.product.png")
        BufferedImage image = ImageIO.read(input.toFile())
        ID id = organizer.write(jobName, jobTimestamp, metadata, image, FileType.PNG)
        assertNotNull(id)
        assertTrue(ID.isValid(id.toString()))
    }

}
