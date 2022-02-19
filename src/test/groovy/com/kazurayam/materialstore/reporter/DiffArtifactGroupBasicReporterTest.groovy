package com.kazurayam.materialstore.reporter

import com.kazurayam.materialstore.MaterialstoreFacade
import com.kazurayam.materialstore.diffartifact.DiffArtifactGroup
import com.kazurayam.materialstore.differ.DiffReporter
import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.JobTimestamp
import com.kazurayam.materialstore.filesystem.MaterialList
import com.kazurayam.materialstore.filesystem.Store
import com.kazurayam.materialstore.filesystem.StoreImpl
import com.kazurayam.materialstore.metadata.IdentifyMetadataValues
import com.kazurayam.materialstore.metadata.IgnoringMetadataKeys
import com.kazurayam.materialstore.metadata.MetadataPattern
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

class DiffArtifactGroupBasicReporterTest {

    private static Path outputDir =
            Paths.get(".").resolve("build/tmp/testOutput")
                    .resolve(DiffArtifactGroupBasicReporterTest.class.getName())

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
        JobName jobName = new JobName("test_reportDiffs")
        // make sure the Job directory to be empty
        FileUtils.deleteDirectory(root.resolve(jobName.toString()).toFile())
        // stuff the Job directory with a fixture
        Path jobNameDir = root.resolve(jobName.toString())
        FileUtils.copyDirectory(resultsDir.toFile(), jobNameDir.toFile())
        //
        JobTimestamp jobTimestamp = new JobTimestamp("20210715_145922")
        // pickup the materials that belongs to the 2 "profiles"
        String profile1 = "ProductionEnv"
        MaterialList left = store.select(jobName, jobTimestamp,
                MetadataPattern.builderWithMap(["profile": profile1 ]).build()
        )

        String profile2 = "DevelopmentEnv"
        MaterialList right = store.select(jobName, jobTimestamp,
                MetadataPattern.builderWithMap(["profile": profile2 ]).build())

        // make diff
        DiffArtifactGroup preparedDAG =
                DiffArtifactGroup.builder(left, right)
                        .ignoreKeys("profile", "URL", "URL.host")
                        .build()
        DiffArtifactGroup stuffedDAG = new MaterialstoreFacade(store).makeDiff(preparedDAG)

        // compile HTML report
        DiffReporter reporter = store.newReporter(jobName)
        Path report = reporter.reportDiffs(stuffedDAG, "index.html")
        assertTrue(Files.exists(report))
    }




    @Test
    void test_decideToBeWarned() {
        assertEquals(false, DiffArtifactGroupBasicReporter.decideToBeWarned(0.00d, 0.0d))
        assertEquals(true, DiffArtifactGroupBasicReporter.decideToBeWarned(1.23d, 0.0d))
        assertEquals(false, DiffArtifactGroupBasicReporter.decideToBeWarned(1.23d, 25.0d))
    }
}
