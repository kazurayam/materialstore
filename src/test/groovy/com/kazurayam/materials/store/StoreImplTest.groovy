package com.kazurayam.materials.store

import com.kazurayam.materials.diff.DiffArtifact
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.jupiter.api.Assertions.*

class StoreImplTest {

    private static Path outputDir =
            Paths.get(".").resolve("build/tmp/testOutput")
                    .resolve(StoreImplTest.class.getName())

    private static Path imagesDir =
            Paths.get(".").resolve("src/test/resources/fixture/sample_images")

    private static Path resultsDir =
            Paths.get(".").resolve("src/test/resources/fixture/sample_results")

    @BeforeAll
    static void beforeAll() {
        Files.createDirectories(outputDir)
    }

    @Test
    void test_getRoot() {
        Path root = outputDir.resolve("Materials")
        Store store = new StoreImpl(root)
        assertTrue(Files.exists(store.getRoot()), "${root} is not present")
        assertEquals("Materials", store.getRoot().getFileName().toString())
    }

    @Test
    void test_getJobResult() {
        Path root = outputDir.resolve("Materials")
        Store store = new StoreImpl(root)
        JobName jobName = new JobName("test_getJob")
        JobTimestamp jobTimestamp = JobTimestamp.now()
        Jobber job = store.getJobber(jobName, jobTimestamp)
        assertNotNull(job)
        assertEquals("test_getJob", job.getJobName().toString())
    }

    /**
     * Test if the Job object cache mechanism in the Organizer object works
     */
    @Test
    void test_getCachedJob() {
        Path root = outputDir.resolve("Materials")
        Store store = new StoreImpl(root)
        JobName jobName = new JobName("test_getCachedJob")
        JobTimestamp jobTimestamp = new JobTimestamp("20210713_093357")
        // make sure the Job directory to be empty
        FileUtils.deleteDirectory(root.resolve(jobName.toString()).toFile())
        // null should be returned if the Job directory is not present or empty
        Jobber expectedNull = store.getCachedJobber(jobName, jobTimestamp)
        assertNull(expectedNull, "expected null but was not")
        // stuff the Job directory with a fixture
        Path jobNameDir = root.resolve(jobName.toString())
        FileUtils.copyDirectory(resultsDir.toFile(), jobNameDir.toFile())
        // new Job object should be created by calling the getJob() method
        Jobber newlyCreatedJob = store.getJobber(jobName, jobTimestamp)
        assertNotNull(newlyCreatedJob, "should not be null")
        // a Job object should be returned from the cache by the getCachedJob() method
        Jobber cachedJob = store.getCachedJobber(jobName, jobTimestamp)
        assertNotNull(cachedJob, "expected non-null but was null")
    }

    @Test
    void test_write_path() {
        Path root = outputDir.resolve("Materials")
        Store store = new StoreImpl(root)
        JobName jobName = new JobName("test_write_path")
        JobTimestamp jobTimestamp = JobTimestamp.now()
        Metadata metadata = new Metadata("DevelopmentEnv", "http://demoaut-mimic.kazurayam.com/")
        Path input = imagesDir.resolve("20210710_142631.development.png")
        Material material = store.write(jobName, jobTimestamp, FileType.PNG, metadata, input)
        assertNotNull(material)
        assertTrue(ID.isValid(material.getIndexEntry().getID().toString()))
    }

    @Test
    void test_select() {
        Path root = outputDir.resolve("Materials")
        Store store = new StoreImpl(root)
        JobName jobName = new JobName("test_select")
        JobTimestamp jobTimestamp = JobTimestamp.now()
        Metadata metadata = new Metadata("DevelopmentEnv", "http://demoaut-mimic.kazurayam.com/")
        Path input = imagesDir.resolve("20210710_142631.development.png")
        Material material = store.write(jobName, jobTimestamp, FileType.PNG, metadata, input)
        //
        MetadataPattern pattern = new MetadataPattern("*", "*")
        List<Material> materials = store.select(jobName, jobTimestamp, FileType.PNG, pattern)
        assertNotNull(materials)
        assertEquals(1, materials.size())
    }

    @Test
    void test_write_File() {
        Path root = outputDir.resolve("Materials")
        Store store = new StoreImpl(root)
        JobName jobName = new JobName("test_write_file")
        JobTimestamp jobTimestamp = JobTimestamp.now()
        Metadata metadata = new Metadata("DevelopmentEnv", "http://demoaut-mimic.kazurayam.com/")
        File input = imagesDir.resolve("20210710_142631.development.png").toFile()
        Material material = store.write(jobName, jobTimestamp, FileType.PNG, metadata, input)
        assertNotNull(material)
        assertTrue(ID.isValid(material.getIndexEntry().getID().toString()))
    }

    @Test
    void test_write_BufferedImage() {
        Path root = outputDir.resolve("Materials")
        Store store = new StoreImpl(root)
        JobName jobName = new JobName("test_write_BufferedImage")
        JobTimestamp jobTimestamp = JobTimestamp.now()
        Metadata metadata = new Metadata("ProductionEnv", "http://demoaut.katalon.com/")
        Path input = imagesDir.resolve("20210710_142628.production.png")
        BufferedImage image = ImageIO.read(input.toFile())
        Material material = store.write(jobName, jobTimestamp, FileType.PNG, metadata, image)
        assertNotNull(material)
        assertTrue(ID.isValid(material.getIndexEntry().getID().toString()))
    }

    @Test
    void test_write_string() {
        Path root = outputDir.resolve("Materials")
        Store store = new StoreImpl(root)
        JobName jobName = new JobName("test_write_String")
        JobTimestamp jobTimestamp = JobTimestamp.now()
        Metadata metadata = new Metadata("ProductionEnv", "http://demoaut.katalon.com/")
        String input = "犬も歩けば棒に当たる"
        Material material = store.write(jobName, jobTimestamp, FileType.TXT, metadata, input)
        assertNotNull(material)
        assertTrue(ID.isValid(material.getIndexEntry().getID().toString()))
    }

    @Test
    void test_findJobbersOf_JobName() {
        Path root = outputDir.resolve("Materials")
        Store store = new StoreImpl(root)
        JobName jobName = new JobName("test_getCachedJob")
        // make sure the Job directory to be empty
        FileUtils.deleteDirectory(root.resolve(jobName.toString()).toFile())
        // stuff the Job directory with a fixture
        Path jobNameDir = root.resolve(jobName.toString())
        FileUtils.copyDirectory(resultsDir.toFile(), jobNameDir.toFile())
        JobTimestamp jobTimestamp = new JobTimestamp("20210713_093357")
        //
        List<Jobber> jobs = store.findJobbersOf(jobName)
        assertNotNull(jobs, "should not be null")
        assertEquals(2, jobs.size())
    }

    @Test
    void test_zipMaterialsToDiff() {
        Path root = outputDir.resolve("Materials")
        Store store = new StoreImpl(root)
        JobName jobName = new JobName("test_zipMaterialsToDiff")
        // make sure the Job directory to be empty
        FileUtils.deleteDirectory(root.resolve(jobName.toString()).toFile())
        // stuff the Job directory with a fixture
        Path jobNameDir = root.resolve(jobName.toString())
        FileUtils.copyDirectory(resultsDir.toFile(), jobNameDir.toFile())
        //
        Jobber jobberOfExpected = store.getJobber(jobName,
                new JobTimestamp("20210713_093357"))
        List<Material> expected = jobberOfExpected.select(FileType.PNG,
                new MetadataPattern("ProductionEnv", "*"))

        assertEquals(1, expected.size())
        //
        Jobber jobberOfActual = store.getJobber(jobName,
                new JobTimestamp("20210715_145922"))
        List<Material> actual= jobberOfActual.select(FileType.PNG,
                new MetadataPattern("DevelopmentEnv", "*"))
        assertEquals(1, actual.size())
        //
        MetadataJoint joint = new MetadataJoint("*", "+")
        List<DiffArtifact> diffArtifacts = store.zipMaterialsToDiff(expected, actual, joint)
        assertNotNull(diffArtifacts)
        assertEquals(3, diffArtifacts.size())
    }

}
