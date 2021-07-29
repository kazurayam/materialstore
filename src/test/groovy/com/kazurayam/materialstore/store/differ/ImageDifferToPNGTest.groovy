package com.kazurayam.materialstore.store.differ

import com.kazurayam.materialstore.TestFixtureUtil
import com.kazurayam.materialstore.store.DiffArtifact
import com.kazurayam.materialstore.store.DiffArtifacts
import com.kazurayam.materialstore.store.FileType
import com.kazurayam.materialstore.store.JobName
import com.kazurayam.materialstore.store.JobTimestamp
import com.kazurayam.materialstore.store.Material
import com.kazurayam.materialstore.store.MetadataIgnoredKeys
import com.kazurayam.materialstore.store.MetadataPattern
import com.kazurayam.materialstore.store.StoreImpl
import groovy.json.JsonOutput
import org.junit.jupiter.api.Test

import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.jupiter.api.Assertions.*

class ImageDifferToPNGTest {

    private static Path outputDir =
            Paths.get(".").resolve("build/tmp/testOutput")
                    .resolve(ImageDifferToPNGTest.class.getName())

    private static Path resultsDir =
            Paths.get(".").resolve("src/test/resources/fixture/sample_results")


    @Test
    void test_makeDiff() {
        Path root = outputDir.resolve("Materials")
        StoreImpl storeImpl = new StoreImpl(root)
        JobName jobName = new JobName("test_makeDiff")
        JobTimestamp jobTimestamp = new JobTimestamp("20210715_145922")
        TestFixtureUtil.setupFixture(storeImpl, jobName)
        //
        List<Material> left = storeImpl.select(jobName, jobTimestamp,
                new MetadataPattern(["profile": "ProductionEnv"]), FileType.PNG)

        List<Material> right = storeImpl.select(jobName, jobTimestamp,
                new MetadataPattern(["profile": "DevelopmentEnv"]), FileType.PNG)

        DiffArtifacts diffArtifacts =
                storeImpl.zipMaterials(left, right,
                        new MetadataIgnoredKeys.Builder()
                                .ignoreKey("profile")
                                .ignoreKey("URL")
                                .ignoreKey("URL.host")
                                .build())
        assertNotNull(diffArtifacts)
        assertEquals(2, diffArtifacts.size(), JsonOutput.prettyPrint(diffArtifacts.toString()))
        //
        DiffArtifact stuffed = new ImageDifferToPNG(root).makeDiffArtifact(diffArtifacts.get(0))
        assertNotNull(stuffed)
        assertNotNull(stuffed.getDiff())
        assertTrue(stuffed.getDiffRatio() > 0)
        assertNotEquals(Material.NULL_OBJECT, stuffed.getDiff())
    }
}