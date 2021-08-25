package com.kazurayam.materialstore


import groovy.json.JsonOutput
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.BeforeAll
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
    void test_write_Path() {
        Path root = outputDir.resolve("Materials")
        Store store = new StoreImpl(root)
        JobName jobName = new JobName("test_write_path")
        JobTimestamp jobTimestamp = JobTimestamp.now()
        Metadata metadata = Metadata.builderWithMap([
                "profile": "DevelopmentEnv",
                "URL": "http://demoaut-mimic.kazurayam.com/"])
                .build()
        Path input = imagesDir.resolve("20210710_142631.development.png")
        Material material = store.write(jobName, jobTimestamp, FileType.PNG, metadata, input)
        assertNotNull(material)
        assertTrue(ID.isValid(material.getIndexEntry().getID().toString()))
    }

    @Test
    void test_select_with_FileType() {
        Path root = outputDir.resolve("Materials")
        Store store = new StoreImpl(root)
        JobName jobName = new JobName("test_select")
        JobTimestamp jobTimestamp = JobTimestamp.now()
        Metadata metadata = Metadata.builderWithMap([
                "profile": "DevelopmentEnv",
                "URL": "http://demoaut-mimic.kazurayam.com/"])
                .build()
        Path input = imagesDir.resolve("20210710_142631.development.png")
        Material material = store.write(jobName, jobTimestamp, FileType.PNG, metadata, input)
        assertNotNull(material)
        //
        MetadataPattern pattern = MetadataPattern.builderWithMap([
                "profile": Pattern.compile(".*"),
                "URL": Pattern.compile(".*")])
                .build()
        // select specifying FileType
        MaterialList materials = store.select(jobName, jobTimestamp, pattern, FileType.PNG)
        assertNotNull(materials)
        assertEquals(1, materials.size())
    }

    @Test
    void test_selectFile() {
        Path root = outputDir.resolve("Materials")
        Store store = new StoreImpl(root)
        JobName jobName = new JobName("test_selectFile")
        JobTimestamp jobTimestamp = JobTimestamp.now()
        Metadata metadata = Metadata.builderWithMap([
                "profile": "DevelopmentEnv",
                "URL": "http://demoaut-mimic.kazurayam.com/"])
                .build()
        Path input = imagesDir.resolve("20210710_142631.development.png")
        Material material = store.write(jobName, jobTimestamp, FileType.PNG, metadata, input)
        assertNotNull(material)
        //
        MetadataPattern pattern = MetadataPattern.builderWithMap([
                "profile": Pattern.compile(".*"),
                "URL": Pattern.compile(".*")])
                .build()
        // select specifying FileType
        File f = store.selectFile(jobName, jobTimestamp, pattern, FileType.PNG)
        assertNotNull(f)
        assertTrue(f.exists())
    }

    @Test
    void test_select_2_files_in_4() {
        Path root = outputDir.resolve("store")
        Store store = new StoreImpl(root)
        JobName jobName = new JobName("test_select_2_files_in_4")
        JobTimestamp jobTimestamp = JobTimestamp.now()
        Path input = imagesDir.resolve("20210710_142631.development.png")
        //
        List<Metadata> metadataList = new ArrayList<Metadata>()
        metadataList.add(Metadata.builderWithMap(["city":"Thanh pho Ho Chi Minh", "country":"VN"]).build())
        metadataList.add(Metadata.builderWithMap(["city":"Tokyo", "country":"JP"]).build())
        metadataList.add(Metadata.builderWithMap(["city":"Prague", "country":"CZ"]).build())
        metadataList.add(Metadata.builderWithMap(["city":"Toronto", "country":"CA"]).build())
        metadataList.forEach { metadata ->
            Material material = store.write(jobName, jobTimestamp, FileType.PNG, metadata, input)
            assertNotNull(material)
        }
        // select 2 members amongst 4 by matching "city" with a Regular Expression `To.*`
        MaterialList selected = store.select(jobName, jobTimestamp,
                MetadataPattern.builderWithMap([
                        "city": Pattern.compile("To.*")])
                        .build());
        assertEquals(2, selected.size())
    }

    @Test
    void test_write_File() {
        Path root = outputDir.resolve("Materials")
        Store store = new StoreImpl(root)
        JobName jobName = new JobName("test_write_file")
        JobTimestamp jobTimestamp = JobTimestamp.now()
        Metadata metadata = Metadata.builderWithMap([
                "profile": "DevelopmentEnv",
                "URL": "http://demoaut-mimic.kazurayam.com/"])
                .build()
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
        Metadata metadata = Metadata.builderWithMap([
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
    void test_write_string() {
        Path root = outputDir.resolve("Materials")
        Store store = new StoreImpl(root)
        JobName jobName = new JobName("test_write_String")
        JobTimestamp jobTimestamp = JobTimestamp.now()
        Metadata metadata = Metadata.builderWithMap([
                "profile": "ProductionEnv",
                "URL": "http://demoaut.katalon.com/"])
                .build()
        String input = "犬も歩けば棒に当たる"
        Material material = store.write(jobName, jobTimestamp, FileType.TXT, metadata, input)
        assertNotNull(material)
        assertTrue(ID.isValid(material.getIndexEntry().getID().toString()))
    }

    @Test
    void test_findAllJobTimestamps() {
        Path root = outputDir.resolve("Materials")
        Store store = new StoreImpl(root)
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
        Path root = outputDir.resolve("Materials")
        Store store = new StoreImpl(root)
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
    void test_findLatestJobTimestamp() {
        Path root = outputDir.resolve("Materials")
        Store store = new StoreImpl(root)
        JobName jobName = new JobName("test_findLatestJobTimestamp")
        TestFixtureUtil.setupFixture(store, jobName)
        //
        JobTimestamp jobTimestamp = store.findLatestJobTimestamp(jobName)
        assertNotNull(jobTimestamp)
        assertNotEquals(JobTimestamp.NULL_OBJECT, jobTimestamp)
        assertEquals(new JobTimestamp("20210715_145922"), jobTimestamp)
    }

    @Test
    void test_findJobTimestampPriorTo() {
        Path root = outputDir.resolve("Materials")
        Store store = new StoreImpl(root)
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
    void test_findJobbersOf_JobName() {
        Path root = outputDir.resolve("Materials")
        Store store = new StoreImpl(root)
        JobName jobName = new JobName("test_getCachedJob")
        // make sure the Job directory to be empty
        FileUtils.deleteDirectory(root.resolve(jobName.toString()).toFile())
        // stuff the Job directory with a fixture
        Path jobNameDir = root.resolve(jobName.toString())
        FileUtils.copyDirectory(resultsDir.toFile(), jobNameDir.toFile())
        //
        List<Jobber> jobs = store.findJobbersOf(jobName)
        assertNotNull(jobs, "should not be null")
        assertEquals(2, jobs.size())
    }

    @Test
    void test_zipMaterials() {
        Path root = outputDir.resolve("store")
        Store store = new StoreImpl(root)
        JobName jobName = new JobName("test_zipMaterials")
        // make sure the Job directory to be empty
        FileUtils.deleteDirectory(root.resolve(jobName.toString()).toFile())
        // stuff the Job directory with a fixture
        Path jobNameDir = root.resolve(jobName.toString())
        FileUtils.copyDirectory(resultsDir.toFile(), jobNameDir.toFile())
        //
        Jobber jobberOfLeft = store.getJobber(jobName,
                new JobTimestamp("20210715_145922"))
        MaterialList leftList = jobberOfLeft.selectMaterials(
                MetadataPattern.builder()
                        .put("profile", "ProductionEnv")
                        .put("URL.file", Pattern.compile(".*"))
                        .build(),
                FileType.PNG)

        //println JsonOutput.prettyPrint(leftList.toString())

        assertEquals(2, leftList.size())
        //
        Jobber jobberOfRight = store.getJobber(jobName,
                new JobTimestamp("20210715_145922"))
            MaterialList rightList= jobberOfRight.selectMaterials(
                MetadataPattern.builder()
                        .put("profile", "DevelopmentEnv")
                        .put("URL.file", Pattern.compile(".*"))
                        .build(),
                    FileType.PNG)
        assertEquals(2, rightList.size())
        //
        DiffArtifacts diffArtifacts =
                store.zipMaterials(leftList, rightList,
                        IgnoringMetadataKeys.of("profile", "URL", "URL.host", "category"))
        assertNotNull(diffArtifacts)
        assertEquals(2, diffArtifacts.size(),
                JsonOutput.prettyPrint(diffArtifacts.toString()))
        assertEquals("""{"URL.file":"/", "xpath":"//a[@id='btn-make-appointment']"}""",
                diffArtifacts.get(0).getDescription())
        assertEquals("""{"URL.file":"/", "xpath":"/html"}""",
                diffArtifacts.get(1).getDescription())

    }

    @Test
    void test_deleteMaterialsOlderThanExclusive() {
        Path root = outputDir.resolve("Materials")
        Store store = new StoreImpl(root)
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
    void test_reportMaterials() {
        Path root = outputDir.resolve("Materials")
        Store store = new StoreImpl(root)
        JobName jobName = new JobName("test_reportMaterials")
        TestFixtureUtil.setupFixture(store, jobName)
        //
        JobTimestamp jobTimestamp = new JobTimestamp("20210715_145922")
        MaterialList materialList = store.select(jobName, jobTimestamp, MetadataPattern.ANY)
        Path report = store.reportMaterials(jobName, materialList, "list.html")
        assertNotNull(report)
        assertTrue(Files.exists(report))
    }

    @Test
    void test_getPathOf() {
        Path root = outputDir.resolve("Materials")
        Store store = new StoreImpl(root)
        JobName jobName = new JobName("test_getAbsolutePathOf")
        TestFixtureUtil.setupFixture(store, jobName)
        JobTimestamp jobTimestamp = new JobTimestamp("20210715_145922")
        MaterialList materialList = store.select(jobName, jobTimestamp, MetadataPattern.ANY)
        Path abs = store.getPathOf(materialList.get(0));
        assertNotNull(abs)
        println abs.toString()
    }

}