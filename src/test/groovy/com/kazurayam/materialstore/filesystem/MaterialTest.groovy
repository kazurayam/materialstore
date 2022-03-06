package com.kazurayam.materialstore.filesystem

import com.kazurayam.materialstore.TestFixtureUtil
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.jupiter.api.Assertions.*

class MaterialTest {

    private final String sampleLine = """6141b40cfe9e7340a483a3097c4f6ff5d20e04ea\tpng\t{"profile":"DevelopmentEnv","URL":"http://demoaut-mimic.kazurayam.com/"}"""

    private static Path outputDir =
            Paths.get(".").resolve("build/tmp/testOutput")
                    .resolve(MaterialTest.class.getName())

    private static Path resultsDir =
            Paths.get(".").resolve("src/test/resources/fixture/sample_results")

    @BeforeAll
    static void beforeAll() {
        if (Files.exists(outputDir)) {
            FileUtils.deleteDirectory(outputDir.toFile())
        }
        Files.createDirectories(outputDir)
    }

    void test_isSimilar() {
        IndexEntry indexEntry = IndexEntry.parseLine(sampleLine)
        JobName jobName = new JobName("test_isSimilar")
        JobTimestamp timestamp0 = new JobTimestamp("20220305_010000")
        JobTimestamp timestamp1 = new JobTimestamp("20220306_235959")
        Material material0 = new Material(jobName, timestamp0, indexEntry)
        Material material1 = new Material(jobName, timestamp1, indexEntry)
        assertEquals(material1, material0)
    }

    @Test
    void test_smoke() {
        IndexEntry indexEntry = IndexEntry.parseLine(sampleLine)
        Material material = new Material(JobName.NULL_OBJECT, JobTimestamp.NULL_OBJECT, indexEntry)
        assertNotNull(material)
        assertEquals("6141b40cfe9e7340a483a3097c4f6ff5d20e04ea",
                material.getIndexEntry().getID().toString())
        assertEquals("6141b40", material.getShortId())
        assertEquals(FileType.PNG, material.getIndexEntry().getFileType())
        assertEquals("""{"URL":"http://demoaut-mimic.kazurayam.com/", "profile":"DevelopmentEnv"}""",
                material.getIndexEntry().getMetadata().toString())
        //
        println material.toString()
        assertEquals(material, material)
        //

    }

    @Test
    void test_getRelativePath_getRelativeURL() {
        Path root = outputDir.resolve("Materials")
        Store store = new StoreImpl(root)
        JobName jobName = new JobName("test_getRelativePath")
        // copy the fixture files to the output dir
        TestFixtureUtil.setupFixture(store, jobName)
        //
        JobTimestamp jobTimestamp = new JobTimestamp("20210713_093357")
        Jobber jobber = new Jobber(root, jobName, jobTimestamp)
        Material material = jobber.selectMaterial(new ID("12a1a5ee4d0ee278ef4998c3f4ebd4951e6d2490"))
        assertNotNull(material)
        //
        Path leftPath = Paths.get("test_getRelativePath/20210713_093357/objects/12a1a5ee4d0ee278ef4998c3f4ebd4951e6d2490.png")
        Path relativePath = material.getRelativePath()
        assertEquals(leftPath, relativePath)
        //
        String leftURL = "test_getRelativePath/20210713_093357/objects/12a1a5ee4d0ee278ef4998c3f4ebd4951e6d2490.png"
        String relativeURL = material.getRelativeURL()
        assertEquals(leftURL, relativeURL)
    }

    @Test
    void test_toFile_and_toURL() {
        Path root = outputDir.resolve("Materials")
        Store store = new StoreImpl(root)
        JobName jobName = new JobName("test_toFile")
        // copy the fixture files to the output dir
        TestFixtureUtil.setupFixture(store, jobName)
        //
        JobTimestamp jobTimestamp = new JobTimestamp("20210713_093357")
        Jobber jobber = new Jobber(root, jobName, jobTimestamp)
        Material material = jobber.selectMaterial(new ID("12a1a5ee4d0ee278ef4998c3f4ebd4951e6d2490"))
        assertNotNull(material)
        //
        File f = material.toFile(root)
        assertTrue(f.exists())
        //
        URL url = material.toURL(root)
        assertTrue(url.toExternalForm().startsWith("file:/"), url.toString())
    }

}