package com.kazurayam.materialstore

import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.jsoup.helper.Validate.fail

/**
 * https://github.com/kazurayam/materialstore/issues/80
 */
class StoreImpl_makeDiffTest {

    static final Path fixtureDir = Paths.get(".")
            .resolve("src/test/resources/fixture/issue#80")
    static final Path outputDir = Paths.get(".")
            .resolve("build/tmp/testOutput")
            .resolve(StoreImpl_makeDiffTest.class.getName())

    static Store store
    static final JobName jobName = new JobName("MyAdmin_visual_inspection_twins")
    static final JobTimestamp timestampP = new JobTimestamp("20220128_191320")
    static final JobTimestamp timestampD = new JobTimestamp("20220128_191342")

    static final String leftUrl = "https://cdn.jsdelivr.net/npm/bootstrap@5.1.0/dist/js/bootstrap.bundle.min.js"
    static final String rightUrl = "https://cdn.jsdelivr.net/npm/bootstrap@5.1.3-rc1/dist/js/bootstrap.bundle.min.js"

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

    /**
     * Issue #80
     * test introducing the IdentifyMetadataValues class
     */
    @Test
    void test_makeDiff() {
        Double criteria = 0.0d
        DiffArtifacts stuffedDiffArtifacts =
                store.makeDiff(left, right,
                        IgnoringMetadataKeys.of("profile", "URL.host"),
                        IdentifyMetadataValues.by(["URL.query":"\\w{32}"]))
        int warnings = stuffedDiffArtifacts.countWarnings(criteria)
        // compile the report
        Path reportFile =
                store.reportDiffs(jobName, stuffedDiffArtifacts, criteria, jobName.toString() + "-index.html")
        assert stuffedDiffArtifacts.size() == 8   // should be 8, not 9
    }


}
