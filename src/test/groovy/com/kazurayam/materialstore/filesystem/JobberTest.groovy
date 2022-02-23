package com.kazurayam.materialstore.filesystem

import com.kazurayam.materialstore.MaterialstoreException
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

import java.util.regex.Pattern

import static org.junit.jupiter.api.Assertions.*

class JobberTest {

    private static Path outputDir =
            Paths.get(".").resolve("build/tmp/testOutput")
                    .resolve(this.getClass().getName())

    private static Path imagesDir =
            Paths.get(".").resolve("src/test/resources/fixture/sample_images")

    private static Path resultsDir =
            Paths.get(".").resolve("src/test/resources/fixture/sample_results")

    private Path root
    private Store store

    @BeforeAll
    static void beforeAll() {
        if (Files.exists(outputDir)) {
            FileUtils.deleteDirectory(outputDir.toFile())
        }
        Files.createDirectories(outputDir)
    }

    @BeforeEach
    void beforeEach() {
        root = outputDir.resolve("store")
        store = new StoreImpl(root)
    }


    @Test
    void test_constructor() {
        JobName jobName = new JobName("test_constructor")
        TestFixtureUtil.setupFixture(store, jobName)
        Jobber jobber = store.getJobber(jobName,
                new JobTimestamp("20210713_093357"))
        assertNotNull(jobber)
        // When constructed, the Jobber object should deserialize the index file from Disk
        assertEquals(3, jobber.size())
    }

    @Test
    void test_read_by_ID() {
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
        JobName jobName = new JobName("test_read_by_Material")
        TestFixtureUtil.setupFixture(store, jobName)
        JobTimestamp jobTimestamp = new JobTimestamp("20210713_093357")
        Jobber jobber = new Jobber(root, jobName, jobTimestamp)
        Material material = jobber.selectMaterial(
                new ID("12a1a5ee4d0ee278ef4998c3f4ebd4951e6d2490"))
        assert material != null
        byte[] data = jobber.read(material)
        assertNotNull(data)
    }

/**
 * selecting a single Material object by ID
 */
    @Test
    void test_selectMaterial_byID() {
        JobName jobName = new JobName("test_selectMaterial")
        TestFixtureUtil.setupFixture(store, jobName)
        JobTimestamp jobTimestamp = new JobTimestamp("20210713_093357")
        Jobber jobber = new Jobber(root, jobName, jobTimestamp)
        Material material = jobber.selectMaterial(
                new ID("12a1a5ee4d0ee278ef4998c3f4ebd4951e6d2490"))
        assertNotNull(material)
    }

    @Test
    void test_selectMaterials_with_FileType() {
        JobName jobName = new JobName("test_selectMaterials_with_FileType")
        JobTimestamp jobTimestamp = JobTimestamp.now()
        Jobber jobber = store.getJobber(jobName, jobTimestamp)
        Metadata metadata = Metadata
                .builderWithUrl(new URL("http://demoaut-mimic.katalon.com/"))
                .put("profile", "DevelopmentEnv")
                .build()
        BufferedImage image =  ImageIO.read(imagesDir.resolve("20210623_225337.development.png").toFile())
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, FileType.PNG.getExtension(), baos);
        byte[] data = baos.toByteArray()
        Material material = jobber.write(data, FileType.PNG, metadata)
        //
        QueryOnMetadata pattern = QueryOnMetadata.builder()
                .put("profile", Pattern.compile(".*"))
                .build()
        MaterialList materialList = jobber.selectMaterials(pattern, FileType.PNG)
        assertNotNull(materialList)
        assertEquals(1, materialList.size())
    }

    @Test
    void test_selectMaterials_without_FileType() {
        JobName jobName = new JobName("test_selectMaterials_without_FileType")
        TestFixtureUtil.setupFixture(store, jobName)
        //
        JobTimestamp jobTimestamp = new JobTimestamp("20210715_145922")
        Jobber jobber = new Jobber(root, jobName, jobTimestamp)
        QueryOnMetadata pattern = QueryOnMetadata.builder()
                .put("profile", "DevelopmentEnv")
                .put("URL", Pattern.compile(".*"))
                .build()
        // select without FileType
        MaterialList materialList = jobber.selectMaterials(pattern)
        assertNotNull(materialList)
        assertTrue(materialList.size() > 0)
    }

    @Test
    void test_selectMaterial_with_QueryOnMetadataANY() {
        JobName jobName = new JobName("test_selectMaterials_without_FileType")
        TestFixtureUtil.setupFixture(store, jobName)
        //
        JobTimestamp jobTimestamp = new JobTimestamp("20210715_145922")
        Jobber jobber = new Jobber(root, jobName, jobTimestamp)
        // select with QueryOnMetadata.ANY, which means all Materials in the job directory
        MaterialList materialList = jobber.selectMaterials(QueryOnMetadata.ANY)
        assertNotNull(materialList)
        assertEquals(6, materialList.size())
    }


    @Test
    void test_write() {
        JobName jobName = new JobName("test_write")
        JobTimestamp jobTimestamp = JobTimestamp.now()
        Jobber jobber = store.getJobber(jobName, jobTimestamp)
        Metadata metadata = Metadata.builderWithMap([
                "profile": "DevelopmentEnv",
                 "URL": "http://demoaut-mimic.katalon.com/"])
                .build()
        BufferedImage image =  ImageIO.read(imagesDir.resolve("20210623_225337.development.png").toFile())
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, FileType.PNG.getExtension(), baos);
        byte[] data = baos.toByteArray()
        Material material = jobber.write(data, FileType.PNG, metadata)
        //
        assertNotNull(material)
    }

    @Test
    void test_write_2_files_of_the_same_bytes_with_different_metadata() {
        JobName jobName = new JobName("test_write_2_files_of_the_same_bytes_with_different_metadata")
        JobTimestamp jobTimestamp = JobTimestamp.now()
        Jobber jobber = store.getJobber(jobName, jobTimestamp)
        byte[] data = "foo".getBytes()
        //
        Metadata metadata1 = Metadata.builderWithMap([
                "profile":"ProductionEnv",
                "URL":"http://example.com"])
                .build()
        Material material1 = jobber.write(data, FileType.TXT, metadata1)
        assert material1 != null
        //
        Metadata metadata2 = Metadata.builderWithMap([
                "profile":"DevelopmentEnv",
                "URL":"http://example.com"])
                .build()
        Material material2 = jobber.write(data, FileType.TXT, metadata2)
        assert material2 != null
    }

    @Test
    void test_write_duplicating_metadata_TERMINATE() {
        JobName jobName = new JobName("test_write_duplicating_metadata_Terminate")
        JobTimestamp jobTimestamp = JobTimestamp.now()
        Jobber jobber = store.getJobber(jobName, jobTimestamp)
        Metadata metadata = Metadata.builderWithMap([
                "profile":"SomeEnv",
                "URL":"http://example.com"])
                .build()
        byte[] data = "foo".getBytes()
        // write one file
        jobber.write(data, FileType.TXT, metadata)
        // try to write one more file with the same metadata with Jobber.DuplicationHandling as default(Terminate)
        MaterialstoreException thrown = assertThrows(MaterialstoreException.class, { ->
            jobber.write(data, FileType.TXT, metadata)
        })
        // try to write one more file with the same metadata with Jobber.DuplicationHandling.Terminate
        MaterialstoreException thrown2 = assertThrows(MaterialstoreException.class, { ->
            jobber.write(data, FileType.TXT, metadata)
        })
        assertTrue(thrown.getMessage().contains("is already there in the index"))
    }

    @Test
    void test_write_duplicating_metadata_CONTINUE() {
        JobName jobName = new JobName("test_write_duplicating_metadata_SkipOne")
        JobTimestamp jobTimestamp = JobTimestamp.now()
        Jobber jobber = store.getJobber(jobName, jobTimestamp)
        Metadata metadata = Metadata.builderWithMap([
                "profile":"SomeEnv",
                "URL":"http://example.com"])
                .build()
        byte[] data = "foo".getBytes()
        // write one file
        jobber.write(data, FileType.TXT, metadata)

        // Now I will try to write one more file with the same metadata
        // with Jobber.DuplicationHandling.SkipOne
        jobber.write(data, FileType.TXT, metadata,
                Jobber.DuplicationHandling.CONTINUE)
        // should continue processing
        // then there should be only one file in the objects directory
        assertEquals(1, jobber.size())
    }




}
