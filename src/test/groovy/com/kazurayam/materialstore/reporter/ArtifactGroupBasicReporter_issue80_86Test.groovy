package com.kazurayam.materialstore.reporter

import com.kazurayam.materialstore.MaterialstoreFacade
import com.kazurayam.materialstore.resolvent.ArtifactGroup
import com.kazurayam.materialstore.differ.DiffReporter
import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.JobTimestamp
import com.kazurayam.materialstore.filesystem.MaterialList
import com.kazurayam.materialstore.metadata.QueryOnMetadata
import com.kazurayam.materialstore.filesystem.Store
import com.kazurayam.materialstore.filesystem.Stores
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.jupiter.api.Assertions.assertTrue

class ArtifactGroupBasicReporter_issue80_86Test {

    static final Path fixtureDir = Paths.get(".")
            .resolve("src/test/resources/fixture/issue#80")
    static final Path outputDir = Paths.get(".")
            .resolve("build/tmp/testOutput")
            .resolve(ArtifactGroupBasicReporter_issue80_86Test.class.getName())

    static Store store
    static final JobName jobName = new JobName("MyAdmin_visual_inspection_twins")
    static final JobTimestamp timestampP = new JobTimestamp("20220128_191320")
    static final JobTimestamp timestampD = new JobTimestamp("20220128_191342")

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
                QueryOnMetadata.builderWithMap(["profile": "MyAdmin_ProductionEnv"]).build()
        )
        assert left.size() == 8
        right = store.select(jobName, timestampD,
                QueryOnMetadata.builderWithMap(["profile": "MyAdmin_DevelopmentEnv"]).build()
        )
        assert right.size() == 8
    }

    /**
     * Issue #80 and #86
     *
     */
    @Test
    void test_reportDiffs() {
        // pick up the materials that belongs to the 2 "profiles"
        String profile1 = "MyAdmin_ProductionEnv"
        MaterialList left = store.select(jobName, timestampP,
                QueryOnMetadata.builderWithMap(["profile": profile1]).build()
        )

        String profile2 = "MyAdmin_DevelopmentEnv"
        MaterialList right = store.select(jobName, timestampD,
                QueryOnMetadata.builderWithMap(["profile": profile2]).build()
        )

        MaterialstoreFacade facade = MaterialstoreFacade.newInstance(store)

        // make diff of the 2 MaterialList objects
        // make diff
        ArtifactGroup preparedAG =
                ArtifactGroup.builder(left, right)
                        .ignoreKeys("profile", "URL", "URL.host")
                        .identifyWithRegex(["URL.query": "\\w{32}"])
                        .build()
        ArtifactGroup reducedAG = facade.reduce(preparedAG)

        // compile HTML report
        DiffReporter reporter = facade.newReporter(jobName)
        Path report = reporter.reportDiffs(reducedAG, "index.html")
        assertTrue(Files.exists(report))

        // test the report content
        String reportText = report.toFile().text

        // make sure the HTML contains a string "class='ignored-key'"
        assertTrue(reportText.contains("class=\'ignored-key\'"),
                "expected \'class=\"ignored-key\"\' in the report but not found")

        // make sure the HTML contains a string "class='matched-value'"
        assertTrue(reportText.contains("class=\'matched-value\'"),
                "expected \'class=\"matched-value\"\' in the report but not found")

        // make sure the HTML contains a string "class='identified-value'"
        assertTrue(reportText.contains("class=\'identified-value\'"),
                "expected a string \'class=\"identified-value\"\' in the report but not found")

        //  issue#86:
        // make sure the HTML contains
        //     <span>"/npm/bootstrap-icons@</span><span class='semantic-version'>1.5.0</span><span>/font/bootstrap-icons.css"</span>
        // for short,
        // a string "<span class='semantic-version'>1." and
        // a string ".0</span>"

        String s1 = "<span class='semantic-version'>1."
        String s2 = ".0</span>"
        assertTrue(reportText.contains(s1) && reportText.contains(s2),
                String.format("expected \"%s\" and \"%s\" in the report but not found", s1, s2))
    }



}
