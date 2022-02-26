package issues.issues

import com.kazurayam.materialstore.MaterialstoreFacade
import com.kazurayam.materialstore.resolvent.ArtifactGroup
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

/**
 * Reproducing the issue #73 at https://github.com/kazurayam/materialstore/issues/73
 * and fixing it.
 */
class Issue73Test {

    static final Path fixtureDir = Paths.get(".")
            .resolve("src/test/resources/fixture/issue#73")
    static final Path outputDir = Paths.get(".")
            .resolve("build/tmp/testOutput")
            .resolve(Issue73Test.class.getName())

    static Store store
    static final JobName jobName = new JobName("MyAdmin_visual_inspection_twins")
    static final JobTimestamp timestampP = new JobTimestamp("20220125_140449")
    static final JobTimestamp timestampD = new JobTimestamp("20220125_140509")

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
                QueryOnMetadata.builderWithMap(["profile": "MyAdmin_ProductionEnv"]).build()
        )
        assert left.size() == 8
        right = store.select(jobName, timestampD,
                QueryOnMetadata.builderWithMap(["profile": "MyAdmin_DevelopmentEnv"]).build()
        )
        assert right.size() == 8
    }

    @Test
    void test_smoke() {
        MaterialstoreFacade facade = MaterialstoreFacade.newInstance(store)
        ArtifactGroup preparedAG =
                ArtifactGroup.builder(left, right)
                        .ignoreKeys("profile", "URL.host")
                        .build()
        ArtifactGroup reducedAG = facade.reduce(preparedAG)
        Double criteria = 0.0d
        int warnings = reducedAG.countWarnings(criteria)
        // compile the report
        Path reportFile = facade.report(jobName, reducedAG, criteria,
                        jobName.toString() + "-index.html")
        assert reducedAG.size() == 8
    }
}
