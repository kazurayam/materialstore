package com.kazurayam.materialstore.store.differ

import com.kazurayam.materialstore.TestFixtureUtil
import com.kazurayam.materialstore.store.*
import org.junit.jupiter.api.Test

import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotEquals
import static org.junit.jupiter.api.Assertions.assertNotNull

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
        List<Material> expected = storeImpl.select(jobName, jobTimestamp,
                new MetadataPattern(["category":"page source","profile": "ProductionEnv"]),
                FileType.HTML)

        List<Material> actual = storeImpl.select(jobName, jobTimestamp,
                new MetadataPattern(["category":"page source","profile": "DevelopmentEnv"]),
                FileType.HTML)

        List<DiffArtifact> diffArtifacts =
                storeImpl.zipMaterials(expected, actual, ["URL.file"] as Set)
        assertNotNull(diffArtifacts)
        assertEquals(1, diffArtifacts.size())
        //
        DiffArtifact stuffed = new TextDifferToMarkdown(root).makeDiffArtifact(diffArtifacts.get(0))
        assertNotNull(stuffed)
        assertNotNull(stuffed.getDiff())
        assertNotEquals(Material.NULL_OBJECT, stuffed.getDiff())
    }
}