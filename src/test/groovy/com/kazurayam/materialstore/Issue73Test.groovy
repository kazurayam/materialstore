package com.kazurayam.materialstore

import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Reproducing the issue #73 at https://github.com/kazurayam/materialstore/issues/73
 * and fixing it.
 */
class Issue73Test {

    static final Path fixtureDir = Paths.get(".")
            .resolve("src/test/resources/fixture/issue#73")
    static final Path outputDir = Paths.get(".")
            .resolve("build/tmp/testOutput")
            .resolve("Issue73Test")
    static Store store
    static final JobName jobName = new JobName("MyAdmin_visual_inspection_twins")
    static final JobTimestamp timestampP = new JobTimestamp("20220125_140449")
    static final JobTimestamp timestampD = new JobTimestamp("20220125_140509")

    MaterialList left
    MaterialList right

    @BeforeAll
    static void beforeAll() {
        if (Files.exists(outputDir)) {
            outputDir.toFile().deleteDir()
        }
        Files.createDirectories(outputDir)
        Path storePath = outputDir.resolve("store")
        FileUtils.copyDirectory(fixtureDir.toFile(), storePath.toFile())
        store = Stores.newInstance(storePath)
    }

    @BeforeEach
    void beforeEach() {
        left = store.select(jobName, timestampP,
                MetadataPattern.builderWithMap(["profile": "MyAdmin_ProductionEnv"]).build()
        )
        assert left.size() == 8
        right = store.select(jobName, timestampD,
                MetadataPattern.builderWithMap(["profile": "MyAdmin_DevelopmentEnv"]).build()
        )
        assert right.size() == 8
    }

    @Test
    void test_smoke() {
        Double criteria = 0.0d
        DiffArtifacts stuffedDiffArtifacts =
                store.makeDiff(left, right, IgnoringMetadataKeys.of("profile", "URL.host"))
        int warnings = stuffedDiffArtifacts.countWarnings(criteria)
        //assert stuffedDiffArtifacts.size() == 8

        // compile the report
        Path reportFile =
                store.reportDiffs(jobName, stuffedDiffArtifacts, criteria, jobName.toString() + "-index.html")
    }
}
