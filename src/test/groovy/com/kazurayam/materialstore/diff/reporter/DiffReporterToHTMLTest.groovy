package com.kazurayam.materialstore.diff.reporter

import com.kazurayam.materialstore.TestFixtureUtil
import com.kazurayam.materialstore.diff.DiffArtifact
import com.kazurayam.materialstore.diff.DiffReporter
import com.kazurayam.materialstore.diff.DifferDriver
import com.kazurayam.materialstore.diff.DifferDriverImpl
import com.kazurayam.materialstore.store.*
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.jupiter.api.Assertions.*

class DiffReporterToHTMLTest {

    private static Path outputDir =
            Paths.get(".").resolve("build/tmp/testOutput")
                    .resolve(DiffReporterToHTMLTest.class.getName())

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
    void test_reportDiffs() {
        Path root = outputDir.resolve("Materials")
        Store store = new StoreImpl(root)
        assert Files.exists(root)
        JobName jobName = new JobName("test_reportDiffs")
        TestFixtureUtil.setupFixture(store, jobName)
        JobTimestamp jobTimestamp = new JobTimestamp("20210715_145922")
        //
        List<Material> expected = store.select(jobName, jobTimestamp,
                new MetadataPattern(["profile": "ProductionEnv"]))

        List<Material> actual = store.select(jobName, jobTimestamp,
                new MetadataPattern(["profile": "DevelopmentEnv"]))

        List<DiffArtifact> input =
                store.zipMaterials(expected, actual, ["URL.file", "category"] as Set)

        // we are going to build a DifferDriver
        DifferDriver differDriver = new DifferDriverImpl.Builder(root).build()

        // now make the diffs
        List<DiffArtifact> stuffed = differDriver.makeDiff(input)
        assertNotNull(stuffed)
        assertEquals(2, stuffed.size())
        //
        DiffReporter diffReporter = new DiffReporterToHTML(root)
        Path reportFile = root.resolve("index.html")
        diffReporter.reportDiffs(stuffed, reportFile)
        //
        assertTrue(Files.exists(reportFile))
        assertTrue(reportFile.toFile().length() > 0)
    }


}
