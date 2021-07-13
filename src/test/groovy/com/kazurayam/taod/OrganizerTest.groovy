package com.kazurayam.taod

import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.jupiter.api.Assertions.*

class OrganizerTest {

    private static Path outputDir =
            Paths.get(".").resolve("build/tmp/testOutput")
                    .resolve(OrganizerTest.class.getName())

    private static Path imagesDir =
            Paths.get(".").resolve("src/test/resources/fixture/sample_images")

    private static Path jobsDir =
            Paths.get(".").resolve("src/test/resources/fixture/sample_jobs")

    @BeforeAll
    static void beforeAll() {
        Files.createDirectories(outputDir)
    }

    @Test
    void test_getRoot() {
        Path root = outputDir.resolve(".taod")
        Organizer organizer = new Organizer(root)
        assertTrue(Files.exists(organizer.getRoot()), "${root} is not present")
        assertEquals(".taod", organizer.getRoot().getFileName().toString())
    }

    @Test
    void test_getJob() {
        Path root = outputDir.resolve(".taod")
        Organizer organizer = new Organizer(root)
        JobName jobName = new JobName("test_getJob")
        JobTimestamp jobTimestamp = JobTimestamp.now()
        Job job = organizer.getJob(jobName, jobTimestamp)
        assertNotNull(job)
        assertEquals("test_getJob", job.getJobName().toString())
    }

    /**
     * Test if the Job object cache mechanism in the Organizer object works
     */
    @Test
    void test_getCachedJob() {
        Path root = outputDir.resolve(".taod")
        Organizer organizer = new Organizer(root)
        JobName jobName = new JobName("test_getCachedJob")
        JobTimestamp jobTimestamp = new JobTimestamp("20210713_093357")
        // make sure the Job directory to be empty
        FileUtils.deleteDirectory(root.resolve(jobName.toString()).toFile())
        // null should be returned if the Job directory is not present or empty
        Job expectedNull = organizer.getCachedJob(jobName, jobTimestamp)
        assertNull(expectedNull, "expected null but was not")
        // stuff the Job directory with a fixture
        Path jobNameDir = root.resolve(jobName.toString())
        FileUtils.copyDirectory(jobsDir.toFile(), jobNameDir.toFile())
        // new Job object should be created by calling the getJob() method
        Job newlyCreatedJob = organizer.getJob(jobName, jobTimestamp)
        assertNotNull(newlyCreatedJob, "should not be null")
        // a Job object should be returned from the cache by the getCachedJob() method
        Job cachedJob = organizer.getCachedJob(jobName, jobTimestamp)
        assertNotNull(cachedJob, "expected non-null but was null")
    }

    @Test
    void test_write_path() {
        Path root = outputDir.resolve(".taod")
        Organizer organizer = new Organizer(root)
        JobName jobName = new JobName("test_write_path")
        JobTimestamp jobTimestamp = JobTimestamp.now()
        Metadata metadata = new Metadata("DevelopEnv", "http://demoaut-mimic.kazurayam.com/")
        Path input = imagesDir.resolve("20210710_142631.develop.png")
        ID id = organizer.write(jobName, jobTimestamp, metadata, input, FileType.PNG)
        assertNotNull(id)
        assertTrue(ID.isValid(id.toString()))
    }


    @Test
    void test_write_File() {
        Path root = outputDir.resolve(".taod")
        Organizer organizer = new Organizer(root)
        JobName jobName = new JobName("test_write_file")
        JobTimestamp jobTimestamp = JobTimestamp.now()
        Metadata metadata = new Metadata("DevelopEnv", "http://demoaut-mimic.kazurayam.com/")
        File input = imagesDir.resolve("20210710_142631.develop.png").toFile()
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
        Path input = imagesDir.resolve("20210710_142628.product.png")
        BufferedImage image = ImageIO.read(input.toFile())
        ID id = organizer.write(jobName, jobTimestamp, metadata, image, FileType.PNG)
        assertNotNull(id)
        assertTrue(ID.isValid(id.toString()))
    }

}
