package com.kazurayam.materialstore.differ

import com.kazurayam.materialstore.*
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
        Path root = outputDir.resolve("Materials")
        StoreImpl storeImpl = new StoreImpl(root)
        JobName jobName = new JobName("test_makeDiff")
        JobTimestamp jobTimestamp = new JobTimestamp("20210715_145922")
        TestFixtureUtil.setupFixture(storeImpl, jobName)
        //
        List<Material> left = storeImpl.select(jobName, jobTimestamp,
                MetadataPattern.builderWithMap([
                        "category":"page source",
                        "profile": "ProductionEnv"])
                        .build(),
                FileType.HTML)

        List<Material> right = storeImpl.select(jobName, jobTimestamp,
                MetadataPattern.builderWithMap([
                        "category":"page source",
                        "profile": "DevelopmentEnv"])
                        .build(),
                FileType.HTML)

        DiffArtifacts diffArtifacts =
                storeImpl.zipMaterials(left, right,
                        new MetadataIgnoredKeys.Builder()
                                .ignoreKey("profile")
                                .ignoreKey("URL")
                                .ignoreKey("URL.host")
                                .build())
        assertNotNull(diffArtifacts)
        assertEquals(1, diffArtifacts.size())
        //
        DiffArtifact stuffed = new TextDifferToMarkdown(root).makeDiffArtifact(diffArtifacts.get(0))
        assertNotNull(stuffed)
        assertNotNull(stuffed.getDiff())
        assertNotEquals(Material.NULL_OBJECT, stuffed.getDiff())
    }
}