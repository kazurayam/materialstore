package com.kazurayam.materialstore.reduce.differ


import com.kazurayam.materialstore.TestFixtureUtil
import com.kazurayam.materialstore.filesystem.FileType
import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.JobTimestamp
import com.kazurayam.materialstore.filesystem.Material
import com.kazurayam.materialstore.filesystem.MaterialList
import com.kazurayam.materialstore.filesystem.QueryOnMetadata
import com.kazurayam.materialstore.filesystem.Store
import com.kazurayam.materialstore.filesystem.StoreImpl
import com.kazurayam.materialstore.reduce.MProductGroup
import com.kazurayam.materialstore.reduce.MaterialProduct
import groovy.json.JsonOutput
import org.junit.jupiter.api.BeforeAll
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

    private static Store store

    @BeforeAll
    static void beforeAll() {
        Path root = outputDir.resolve("store")
        store = new StoreImpl(root)
    }

    @Test
    void test_injectDiff() {
        JobName jobName = new JobName("test_makeDiff")
        JobTimestamp jobTimestamp = new JobTimestamp("20210715_145922")
        TestFixtureUtil.setupFixture(store, jobName)
        MaterialList left = store.select(jobName, jobTimestamp,
                QueryOnMetadata.builder(["profile": "ProductionEnv"]).build(),
                FileType.PNG)
        MaterialList right = store.select(jobName, jobTimestamp,
                QueryOnMetadata.builder(["profile": "DevelopmentEnv"]).build(),
                FileType.PNG)
        MProductGroup mProductGroup =
                MProductGroup.builder(left, right)
                        .ignoreKeys("profile", "URL", "URL.host")
                        .build()
        assertNotNull(mProductGroup)
        assertEquals(2, mProductGroup.size(), JsonOutput.prettyPrint(mProductGroup.toString()))
        //
        MaterialProduct stuffed = new ImageDifferToPNG(store).injectDiff(mProductGroup.get(0))
        assertNotNull(stuffed)
        assertNotNull(stuffed.getDiff())
        assertTrue(stuffed.getDiffRatio() > 0)
        assertNotEquals(Material.NULL_OBJECT, stuffed.getDiff())
    }
}