package com.kazurayam.materialstore.store.reporter


import com.kazurayam.materialstore.store.*
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

class DiffArtifactsBasicReporterTest {

    private static Path outputDir =
            Paths.get(".").resolve("build/tmp/testOutput")
                    .resolve(DiffArtifactsBasicReporterTest.class.getName())

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
        List<Material> left = store.select(jobName, jobTimestamp,
                new MetadataPattern([ "profile": profile1 ]))

        String profile2 = "DevelopmentEnv"
        List<Material> right = store.select(jobName, jobTimestamp,
                new MetadataPattern([ "profile": profile2 ]))

        // make diff
        DiffArtifacts stuffedDiffArtifacts =
                store.makeDiff(left, right,
                        new MetadataIgnoredKeys.Builder()
                                .ignoreKey("profile")
                                .ignoreKey("URL")
                                .ignoreKey("URL.host")
                                .build())

        // compile HTML report
        DiffReporter reporter = store.newReporter(jobName)
        int warnCount = reporter.reportDiffs(stuffedDiffArtifacts, "index.html")
        assertTrue(warnCount > 0)

        Path reportFile = root.resolve("index.html")
        assertTrue(Files.exists(reportFile))
    }

    @Test
    void test_decideToBeWarned() {
        assertEquals(false, DiffArtifactsBasicReporter.decideToBeWarned(0.00d, 0.0d))
        assertEquals(true, DiffArtifactsBasicReporter.decideToBeWarned(1.23d, 0.0d))
        assertEquals(false, DiffArtifactsBasicReporter.decideToBeWarned(1.23d, 25.0d))
    }
}
