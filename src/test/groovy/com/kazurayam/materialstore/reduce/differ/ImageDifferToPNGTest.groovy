package com.kazurayam.materialstore.reduce.differ


import com.kazurayam.materialstore.TestFixtureUtil
import com.kazurayam.materialstore.filesystem.*
import com.kazurayam.materialstore.metadata.QueryOnMetadata
import com.kazurayam.materialstore.reduce.MProduct
import com.kazurayam.materialstore.reduce.MProductGroup
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
        Path root = outputDir.resolve("store")
        StoreImpl storeImpl = new StoreImpl(root)
        JobName jobName = new JobName("test_makeDiff")
        JobTimestamp jobTimestamp = new JobTimestamp("20210715_145922")
        TestFixtureUtil.setupFixture(storeImpl, jobName)
        //
        MaterialList left = storeImpl.select(jobName, jobTimestamp,
                QueryOnMetadata.builder(["profile": "ProductionEnv"]).build(),
                FileType.PNG)

        MaterialList right = storeImpl.select(jobName, jobTimestamp,
                QueryOnMetadata.builder(["profile": "DevelopmentEnv"]).build(),
                FileType.PNG)

        MProductGroup mProductGroup =
                MProductGroup.builder(left, right)
                        .ignoreKeys("profile", "URL", "URL.host")
                        .build()
        assertNotNull(mProductGroup)
        assertEquals(2, mProductGroup.size(), JsonOutput.prettyPrint(mProductGroup.toString()))
        //
        MProduct stuffed = new ImageDifferToPNG(root).makeMProduct(mProductGroup.get(0))
        assertNotNull(stuffed)
        assertNotNull(stuffed.getDiff())
        assertTrue(stuffed.getDiffRatio() > 0)
        assertNotEquals(Material.NULL_OBJECT, stuffed.getDiff())
    }
}