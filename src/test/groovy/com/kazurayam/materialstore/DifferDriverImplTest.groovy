package com.kazurayam.materialstore


import com.kazurayam.materialstore.differ.ImageDifferToPNG
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.jupiter.api.Assertions.*

class DifferDriverImplTest {

    private static Path outputDir =
            Paths.get(".").resolve("build/tmp/testOutput")
                    .resolve(DifferDriverImplTest.class.getName())

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
    void test_ImageDiffer() {
        Path root = outputDir.resolve("Materials")
        StoreImpl storeImpl = new StoreImpl(root)
        assert Files.exists(root)
        JobName jobName = new JobName("test_ImageDiffer")
        TestFixtureUtil.setupFixture(storeImpl, jobName)
        JobTimestamp jobTimestamp = new JobTimestamp("20210715_145922")
        //
        List<Material> left = storeImpl.select(jobName, jobTimestamp,
                new MetadataPattern.Builder(["profile": "ProductionEnv"]).build(),
                FileType.PNG)

        List<Material> right = storeImpl.select(jobName, jobTimestamp,
                new MetadataPattern.Builder(["profile": "DevelopmentEnv"]).build(),
                FileType.PNG)

        DiffArtifacts input =
                storeImpl.zipMaterials(left, right,
                        new MetadataIgnoredKeys.Builder()
                                .ignoreKey("profile")
                                .ignoreKey("URL")
                                .ignoreKey("URL.host")
                                .build())
        //
        DifferDriver differDriver = new DifferDriverImpl.Builder(root).build()
        DiffArtifacts stuffed = differDriver.differentiate(input)
        assertNotNull(stuffed)
        assertEquals(2, stuffed.size())
    }

    @Test
    void test_Builder_differFor() {
        Path root = outputDir.resolve("Materials")
        Store store = new StoreImpl(root)
        DifferDriver differDriver = new DifferDriverImpl.Builder(root)
                .differFor(FileType.JPEG, new ImageDifferToPNG())
                .build()
        assertTrue(differDriver.hasDiffer(FileType.JPEG))
    }

    @Test
    void test_TextDiffer() {
        Path root = outputDir.resolve("Materials")
        StoreImpl storeImpl = new StoreImpl(root)
        JobName jobName = new JobName("test_TextDiffer")
        TestFixtureUtil.setupFixture(storeImpl, jobName)
        JobTimestamp jobTimestamp = new JobTimestamp("20210715_145922")
        //
        List<Material> left = storeImpl.select(jobName, jobTimestamp,
                new MetadataPattern.Builder(["profile": "ProductionEnv"]).build(),
                FileType.HTML)

        List<Material> right = storeImpl.select(jobName, jobTimestamp,
                new MetadataPattern.Builder(["profile": "DevelopmentEnv"]).build(),
                FileType.HTML)

        DiffArtifacts input =
                storeImpl.zipMaterials(left, right,
                        new MetadataIgnoredKeys.Builder()
                                .ignoreKey("profile")
                                .ignoreKey("URL")
                                .ignoreKey("URL.host")
                                .build())
        //
        DifferDriver differDriver = new DifferDriverImpl.Builder(root).build()
        DiffArtifacts stuffed = differDriver.differentiate(input)
        assertNotNull(stuffed)
        assertEquals(1, stuffed.size())
    }

}
