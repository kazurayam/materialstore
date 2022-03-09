package com.kazurayam.materialstore.reduce.differ


import com.kazurayam.materialstore.TestFixtureUtil
import com.kazurayam.materialstore.filesystem.*
import com.kazurayam.materialstore.reduce.MProduct
import com.kazurayam.materialstore.reduce.MProductGroup
import org.junit.jupiter.api.Test

import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.jupiter.api.Assertions.*

class TextDifferToMarkdownTest {

    private static Path outputDir =
            Paths.get(".").resolve("build/tmp/testOutput")
                    .resolve(TextDifferToMarkdownTest.class.getName())

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
                QueryOnMetadata.builder([
                        "category":"page source",
                        "profile": "ProductionEnv"])
                        .build(),
                FileType.HTML)

        MaterialList right = storeImpl.select(jobName, jobTimestamp,
                QueryOnMetadata.builder([
                        "category":"page source",
                        "profile": "DevelopmentEnv"])
                        .build(),
                FileType.HTML)

        MProductGroup mProductGroup =
                MProductGroup.builder(left, right)
                        .ignoreKeys("profile", "URL", "URL.host")
                        .build()
        assertNotNull(mProductGroup)
        assertEquals(1, mProductGroup.size())
        //
        MProduct stuffed = new TextDifferToMarkdown(root).makeMProduct(mProductGroup.get(0))
        assertNotNull(stuffed)
        assertNotNull(stuffed.getDiff())
        assertNotEquals(Material.NULL_OBJECT, stuffed.getDiff())
    }
}