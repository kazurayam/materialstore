package com.kazurayam.materialstore.reporter

import com.kazurayam.materialstore.MaterialstoreFacade
import com.kazurayam.materialstore.resolvent.ArtifactGroup
import com.kazurayam.materialstore.differ.DiffReporter
import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.JobTimestamp
import com.kazurayam.materialstore.filesystem.MaterialList
import com.kazurayam.materialstore.filesystem.Store
import com.kazurayam.materialstore.filesystem.StoreImpl
import com.kazurayam.materialstore.metadata.QueryOnMetadata
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

class ArtifactGroupBasicReporterTest {

    private static Path outputDir =
            Paths.get(".").resolve("build/tmp/testOutput")
                    .resolve(ArtifactGroupBasicReporterTest.class.getName())

    private static Path resultsDir =
            Paths.get(".").resolve("src/test/fixture/sample_results")

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
                QueryOnMetadata.builderWithMap(["profile": profile1 ]).build()
        )

        String profile2 = "DevelopmentEnv"
        MaterialList right = store.select(jobName, jobTimestamp,
                QueryOnMetadata.builderWithMap(["profile": profile2 ]).build())

        MaterialstoreFacade facade = MaterialstoreFacade.newInstance(store)

        // make diff
        ArtifactGroup preparedAG =
                ArtifactGroup.builder(left, right)
                        .ignoreKeys("profile", "URL", "URL.host")
                        .build()
        ArtifactGroup reducedAG = facade.reduce(preparedAG)

        // compile HTML report
        DiffReporter reporter = facade.newReporter(jobName)
        Path report = reporter.reportDiffs(reducedAG, "index.html")
        assertTrue(Files.exists(report))
    }




    @Test
    void test_decideToBeWarned() {
        assertEquals(false, ArtifactGroupBasicReporter.decideToBeWarned(0.00d, 0.0d))
        assertEquals(true, ArtifactGroupBasicReporter.decideToBeWarned(1.23d, 0.0d))
        assertEquals(false, ArtifactGroupBasicReporter.decideToBeWarned(1.23d, 25.0d))
    }
}
