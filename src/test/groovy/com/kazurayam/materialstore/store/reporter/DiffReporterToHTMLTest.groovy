package com.kazurayam.materialstore.store.reporter

import com.kazurayam.materialstore.TestFixtureUtil
import com.kazurayam.materialstore.store.differ.TextDifferToHTML
import com.kazurayam.materialstore.store.*
import groovy.xml.MarkupBuilder
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
        JobName jobName = new JobName("test_smoke")
        // make sure the Job directory to be empty
        FileUtils.deleteDirectory(root.resolve(jobName.toString()).toFile())
        // stuff the Job directory with a fixture
        Path jobNameDir = root.resolve(jobName.toString())
        FileUtils.copyDirectory(resultsDir.toFile(), jobNameDir.toFile())
        //
        JobTimestamp jobTimestamp = new JobTimestamp("20210715_145922")
        // pickup the materials that belongs to the 2 "profiles"
        String profile1 = "ProductionEnv"
        List<Material> expected = store.select(jobName, jobTimestamp,
                new MetadataPattern([ "profile": profile1 ]))

        String profile2 = "DevelopmentEnv"
        List<Material> actual = store.select(jobName, jobTimestamp,
                new MetadataPattern([ "profile": profile2 ]))

        // make diff
        List<DiffArtifact> stuffedDiffArtifacts =
                store.makeDiff(expected, actual, ["URL.file"] as Set)

        // compile HTML report
        DiffReporter reporter = store.newReporter(jobName)
        reporter.reportDiffs(stuffedDiffArtifacts, "index.html")

        Path reportFile = root.resolve("index.html")
        assertTrue(Files.exists(reportFile))
    }
}
