package com.kazurayam.materialstore.filesystem

import com.kazurayam.materialstore.TestFixtureUtil
import com.kazurayam.materialstore.metadata.Metadata
import com.kazurayam.materialstore.metadata.QueryOnMetadata
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.temporal.ChronoUnit
import java.util.regex.Pattern

import static org.junit.jupiter.api.Assertions.*

class StoreImplTest {

    private static Path outputDir =
            Paths.get(".").resolve("build/tmp/testOutput")
                    .resolve(StoreImplTest.class.getName())

    private static Path imagesDir =
            Paths.get(".").resolve("src/test/fixture/sample_images")

    private static Path resultsDir =
            Paths.get(".").resolve("src/test/fixture/sample_results")

    static Boolean verbose = true

    Path root
    Store store

    @BeforeAll
    static void beforeAll() {
        if (Files.exists(outputDir)) {
            FileUtils.deleteDirectory(outputDir.toFile())
        }
        Files.createDirectories(outputDir)
        // if verbose logging required, change the log level
        if (verbose) {
            System.setProperty("org.slf4j.simpleLogger.log.com.kazurayam.materialstore.filesystem.StoreImpl", "DEBUG")
        }
    }

    @BeforeEach
    void setup() {
        root = outputDir.resolve("store")
        store = new StoreImpl(root)
    }

    @Test
    void test_deleteMaterialsOlderThanExclusive() {
        JobName jobName = new JobName("test_deleteMaterialsOlderThanExclusive")
        TestFixtureUtil.setupFixture(store, jobName)
        //
        JobTimestamp latestTimestamp = new JobTimestamp("20210715_145922")
        int deletedFiles = store.deleteMaterialsOlderThanExclusive(jobName,
                latestTimestamp, 0L, ChronoUnit.DAYS)
        assertEquals(6, deletedFiles)
        /* deleted 6 files include
         * - 20210713_093357/objects/12a1a5e ...
         * - 20210713_093357/objects/6141b60 ...
         * - 20210713_093357/objects/ab56d30 ...
         * - 20210713_093357/objects/
         * - 20210713_093357/index
         * - 20210713_093357/
         */
    }

    @Test
    void test_findAllJobTimestamps() {
        JobName jobName = new JobName("test_findAllJobTimestamps")
        TestFixtureUtil.setupFixture(store, jobName)
        //
        List<JobTimestamp> jobTimestamps = store.findAllJobTimestamps(jobName)
        assertNotNull(jobTimestamps)
        assertEquals(2, jobTimestamps.size())
        assertEquals(new JobTimestamp("20210715_145922"), jobTimestamps.get(0))
        assertEquals(new JobTimestamp("20210713_093357"), jobTimestamps.get(1))
    }

    @Test
    void test_findAllJobTimestampsPriorTo() {
        JobName jobName = new JobName("test_findAllJobTimestampsPriorTo")
        TestFixtureUtil.setupFixture(store, jobName)
        //
        JobTimestamp jobTimestamp = new JobTimestamp("20210715_145922")
        List<JobTimestamp> jobTimestamps = store.findAllJobTimestampsPriorTo(jobName, jobTimestamp)
        assertNotNull(jobTimestamps)
        assertEquals(1, jobTimestamps.size())
        assertEquals(new JobTimestamp("20210713_093357"), jobTimestamps.get(0))
    }


    @Test
    void test_findJobTimestampPriorTo() {
        JobName jobName = new JobName("test_findLatestJobTimestamp")
        TestFixtureUtil.setupFixture(store, jobName)
        //
        JobTimestamp latest = store.findLatestJobTimestamp(jobName)
        JobTimestamp second = store.findJobTimestampPriorTo(jobName, latest)
        assertNotNull(second)
        assertNotEquals(JobTimestamp.NULL_OBJECT, second)
        assertEquals(new JobTimestamp("20210713_093357"), second)
    }


    @Test
    void test_findLatestJobTimestamp() {
        JobName jobName = new JobName("test_findLatestJobTimestamp")
        TestFixtureUtil.setupFixture(store, jobName)
        //
        JobTimestamp jobTimestamp = store.findLatestJobTimestamp(jobName)
        assertNotNull(jobTimestamp)
        assertNotEquals(JobTimestamp.NULL_OBJECT, jobTimestamp)
        assertEquals(new JobTimestamp("20210715_145922"), jobTimestamp)
    }

    /**
     * Test if the Job object cache mechanism in the Organizer object works
     */
    @Test
    void test_getCachedJob() {
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
    void test_getJobResult() {
        JobName jobName = new JobName("test_getJob")
        JobTimestamp jobTimestamp = JobTimestamp.now()
        Jobber job = store.getJobber(jobName, jobTimestamp)
        assertNotNull(job)
        assertEquals("test_getJob", job.getJobName().toString())
    }

    @Test
    void test_getPathOf() {
        JobName jobName = new JobName("test_getAbsolutePathOf")
        TestFixtureUtil.setupFixture(store, jobName)
        JobTimestamp jobTimestamp = new JobTimestamp("20210715_145922")
        MaterialList materialList = store.select(jobName, jobTimestamp, QueryOnMetadata.ANY)
        Path abs = store.getPathOf(materialList.get(0))
        assertNotNull(abs)
        println abs.toString()
    }

    @Test
    void test_getRoot() {
        assertTrue(Files.exists(store.getRoot()), "${root} is not present")
        assertEquals("store", store.getRoot().getFileName().toString())
    }

    @Test
    void test_queryAllJobTimestamp() {
        JobName jobName = new JobName("test_queryAllJobTimestamps")
        TestFixtureUtil.setupFixture(store, jobName)
        //
        URL url = new URL("http://demoaut-mimic.kazurayam.com/")
        Metadata metadata = new Metadata.Builder().putAll([
                "URL.host":"demoaut-mimic.kazurayam.com",
                "profile":"DevelopmentEnv"
        ]).build()
        QueryOnMetadata query = new QueryOnMetadata.Builder(metadata).build()
        List<JobTimestamp> jobTimestamps =
                store.queryAllJobTimestamps(jobName, query)
        assertNotNull(jobTimestamps)
        assertEquals(2, jobTimestamps.size())
        assertEquals(new JobTimestamp("20210715_145922"), jobTimestamps.get(0))
        assertEquals(new JobTimestamp("20210713_093357"), jobTimestamps.get(1))
    }

    @Test
    void test_queryAllJobTimestampPriorTo() {
        JobName jobName = new JobName("test_queryAllJobTimestampsPriorTo")
        TestFixtureUtil.setupFixture(store, jobName)
        //
        URL url = new URL("http://demoaut-mimic.kazurayam.com/")
        Metadata metadata = new Metadata.Builder().putAll([
                "URL.host":"demoaut-mimic.kazurayam.com",
                "profile":"DevelopmentEnv"
        ]).build()
        QueryOnMetadata query = new QueryOnMetadata.Builder(metadata).build()
        JobTimestamp jobTimestamp = new JobTimestamp("20210715_145922")
        List<JobTimestamp> jobTimestamps =
                store.queryAllJobTimestampsPriorTo(jobName, query, jobTimestamp)
        assertNotNull(jobTimestamps)
        assertEquals(1, jobTimestamps.size())
        assertEquals(new JobTimestamp("20210713_093357"), jobTimestamps.get(0))
    }

    @Test
    void test_queryJobTimestampPriorTo() {
        JobName jobName = new JobName("test_queryJobTimestampPriorTo")
        TestFixtureUtil.setupFixture(store, jobName)
        //
        URL url = new URL("http://demoaut-mimic.kazurayam.com/")
        Metadata metadata = new Metadata.Builder().putAll([
                "URL.host":"demoaut-mimic.kazurayam.com",
                "profile":"DevelopmentEnv"
        ]).build()
        QueryOnMetadata query = new QueryOnMetadata.Builder(metadata).build()
        JobTimestamp jobTimestamp = new JobTimestamp("20210715_145922")
        JobTimestamp found =
                store.queryJobTimestampPriorTo(jobName, query, jobTimestamp)
        assertNotNull(found)
        assertNotEquals(JobTimestamp.NULL_OBJECT, found)
        assertEquals(new JobTimestamp("20210713_093357"), found)
    }


    @Test
    void test_queryLatestJobTimestamp() {
        JobName jobName = new JobName("test_queryLatestJobTimestamp")
        TestFixtureUtil.setupFixture(store, jobName)
        //
        URL url = new URL("http://demoaut-mimic.kazurayam.com/")
        Metadata metadata = new Metadata.Builder().putAll([
                "URL.host":"demoaut-mimic.kazurayam.com",
                "profile":"DevelopmentEnv"
        ]).build()
        QueryOnMetadata query = new QueryOnMetadata.Builder(metadata).build()
        JobTimestamp found =
                store.queryLatestJobTimestamp(jobName, query)
        assertNotNull(found)
        assertNotEquals(JobTimestamp.NULL_OBJECT, found)
        assertEquals(new JobTimestamp("20210715_145922"), found)
    }

    @Test
    void test_read_material() {
        JobName jobName = new JobName("test_read_material")
        JobTimestamp jobTimestamp = JobTimestamp.now()
        Metadata metadata = Metadata.builder([
                "profile": "DevelopmentEnv",
                "URL": "http://demoaut-mimic.kazurayam.com/"])
                .build()
        Path input = imagesDir.resolve("20210710_142631.development.png")
        Material material = store.write(jobName, jobTimestamp, FileType.PNG, metadata, input)
        //
        byte[] bytes = store.read(material)
        assertTrue(bytes.length > 0)
    }

    @Test
    void test_select_2_files_in_4() {
        JobName jobName = new JobName("test_select_2_files_in_4")
        JobTimestamp jobTimestamp = JobTimestamp.now()
        Path input = imagesDir.resolve("20210710_142631.development.png")
        //
        List<Metadata> metadataList = new ArrayList<Metadata>()
        metadataList.add(Metadata.builder(["city":"Thanh pho Ho Chi Minh", "country":"VN"]).build())
        metadataList.add(Metadata.builder(["city":"Tokyo", "country":"JP"]).build())
        metadataList.add(Metadata.builder(["city":"Prague", "country":"CZ"]).build())
        metadataList.add(Metadata.builder(["city":"Toronto", "country":"CA"]).build())
        metadataList.forEach { metadata ->
            Material material = store.write(jobName, jobTimestamp, FileType.PNG, metadata, input)
            assertNotNull(material)
        }
        // select 2 members amongst 4 by matching "city" with a Regular Expression `To.*`
        MaterialList selected = store.select(jobName, jobTimestamp,
                QueryOnMetadata.builder()
                        .put("city", Pattern.compile("To.*"))
                        .build())
        assertEquals(2, selected.size())
    }

    @Test
    void test_select_with_FileType() {
        JobName jobName = new JobName("test_select")
        JobTimestamp jobTimestamp = JobTimestamp.now()
        Metadata metadata = Metadata.builder([
                "profile": "DevelopmentEnv",
                "URL": "http://demoaut-mimic.kazurayam.com/"])
                .build()
        Path input = imagesDir.resolve("20210710_142631.development.png")
        Material material = store.write(jobName, jobTimestamp, FileType.PNG, metadata, input)
        assertNotNull(material)
        //
        QueryOnMetadata pattern = QueryOnMetadata.builder()
                .put("profile", Pattern.compile(".*"))
                .put("URL", Pattern.compile(".*"))
                .build()
        // select specifying FileType
        MaterialList materials = store.select(jobName, jobTimestamp, pattern, FileType.PNG)
        assertNotNull(materials)
        assertEquals(1, materials.size())
    }

    @Test
    void test_selectFile() {
        JobName jobName = new JobName("test_selectFile")
        JobTimestamp jobTimestamp = JobTimestamp.now()
        Metadata metadata = Metadata.builder([
                "profile": "DevelopmentEnv",
                "URL": "http://demoaut-mimic.kazurayam.com/"])
                .build()
        Path input = imagesDir.resolve("20210710_142631.development.png")
        Material material = store.write(jobName, jobTimestamp, FileType.PNG, metadata, input)
        assertNotNull(material)
        //
        QueryOnMetadata pattern = QueryOnMetadata.builder()
                .put("profile", Pattern.compile(".*"))
                .put("URL", Pattern.compile(".*"))
                .build()
        // select specifying FileType
        File f = store.selectFile(jobName, jobTimestamp, pattern, FileType.PNG)
        assertNotNull(f)
        assertTrue(f.exists())
    }

    @Test
    void test_write_BufferedImage() {
        JobName jobName = new JobName("test_write_BufferedImage")
        JobTimestamp jobTimestamp = JobTimestamp.now()
        Metadata metadata = Metadata.builder([
                "profile": "ProductionEnv",
                "URL": "http://demoaut.katalon.com/"])
                .build()
        Path input = imagesDir.resolve("20210710_142628.production.png")
        BufferedImage image = ImageIO.read(input.toFile())
        Material material = store.write(jobName, jobTimestamp, FileType.PNG, metadata, image)
        assertNotNull(material)
        assertTrue(ID.isValid(material.getIndexEntry().getID().toString()))
    }

    @Test
    void test_write_File() {
        JobName jobName = new JobName("test_write_file")
        JobTimestamp jobTimestamp = JobTimestamp.now()
        Metadata metadata = Metadata.builder([
                "profile": "DevelopmentEnv",
                "URL": "http://demoaut-mimic.kazurayam.com/"])
                .build()
        File input = imagesDir.resolve("20210710_142631.development.png").toFile()
        Material material = store.write(jobName, jobTimestamp, FileType.PNG, metadata, input)
        assertNotNull(material)
        assertTrue(ID.isValid(material.getIndexEntry().getID().toString()))
    }

    @Test
    void test_write_Path() {
        JobName jobName = new JobName("test_write_path")
        JobTimestamp jobTimestamp = JobTimestamp.now()
        Metadata metadata = Metadata.builder([
                "profile": "DevelopmentEnv",
                "URL": "http://demoaut-mimic.kazurayam.com/"])
                .build()
        Path input = imagesDir.resolve("20210710_142631.development.png")
        Material material = store.write(jobName, jobTimestamp, FileType.PNG, metadata, input)
        assertNotNull(material)
        assertTrue(ID.isValid(material.getIndexEntry().getID().toString()))
    }

    @Test
    void test_write_string() {
        JobName jobName = new JobName("test_write_String")
        JobTimestamp jobTimestamp = JobTimestamp.now()
        Metadata metadata = Metadata.builder([
                "profile": "ProductionEnv",
                "URL": "http://demoaut.katalon.com/"])
                .build()
        String input = "犬も歩けば棒に当たる"
        Material material = store.write(jobName, jobTimestamp, FileType.TXT, metadata, input)
        assertNotNull(material)
        assertTrue(ID.isValid(material.getIndexEntry().getID().toString()))
    }
}