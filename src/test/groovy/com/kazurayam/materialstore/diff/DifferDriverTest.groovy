package com.kazurayam.materialstore.diff

import com.kazurayam.materialstore.store.FileType
import com.kazurayam.materialstore.store.JobName
import com.kazurayam.materialstore.store.JobTimestamp
import com.kazurayam.materialstore.store.Material
import com.kazurayam.materialstore.store.MetadataPattern
import com.kazurayam.materialstore.store.StoreImpl
import org.junit.jupiter.api.Test

import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.jupiter.api.Assertions.*

class DifferDriverTest {

    private static Path outputDir =
            Paths.get(".").resolve("build/tmp/testOutput")
                    .resolve(DifferDriverTest.class.getName())

    private static Path resultsDir =
            Paths.get(".").resolve("src/test/resources/fixture/sample_results")


    @Test
    void test_PNGDiffer() {
        Path root = outputDir.resolve("Materials")
        StoreImpl storeImpl = new StoreImpl(root)
        JobName jobName = new JobName("test_PNGDiffer")
        JobTimestamp jobTimestamp = new JobTimestamp("20210715_145922")
        DiffTestUtil.setupFixture(storeImpl, jobName, jobTimestamp)
        //
        List<Material> expected = storeImpl.select(jobName, jobTimestamp, FileType.PNG,
                new MetadataPattern(["profile": "ProductionEnv"]))

        List<Material> actual = storeImpl.select(jobName, jobTimestamp, FileType.PNG,
                new MetadataPattern(["profile": "DevelopmentEnv"]))

        List<DiffArtifact> input =
                storeImpl.zipMaterials(expected, actual, ["URL.file"] as Set)
        //
        List<DiffArtifact> stuffed = DifferDriver.makeDiff(input)
        assertNotNull(stuffed)
        assertEquals(1, stuffed.size())
    }


    @Test
    void test_HTMLDiffer() {
        Path root = outputDir.resolve("Materials")
        StoreImpl storeImpl = new StoreImpl(root)
        JobName jobName = new JobName("test_HTMLDiffer")
        JobTimestamp jobTimestamp = new JobTimestamp("20210715_145922")
        DiffTestUtil.setupFixture(storeImpl, jobName, jobTimestamp)
        //
        List<Material> expected = storeImpl.select(jobName, jobTimestamp, FileType.HTML,
                new MetadataPattern(["profile": "ProductionEnv"]))

        List<Material> actual = storeImpl.select(jobName, jobTimestamp, FileType.HTML,
                new MetadataPattern(["profile": "DevelopmentEnv"]))

        List<DiffArtifact> input =
                storeImpl.zipMaterials(expected, actual, ["URL.file"] as Set)
        //
        List<DiffArtifact> stuffed = DifferDriver.makeDiff(input)
        assertNotNull(stuffed)
        assertEquals(1, stuffed.size())
    }
}
