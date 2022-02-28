package com.kazurayam.materialstore.resolvent

import com.kazurayam.materialstore.filesystem.FileType
import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.JobTimestamp
import com.kazurayam.materialstore.filesystem.Jobber
import com.kazurayam.materialstore.filesystem.Material
import com.kazurayam.materialstore.filesystem.MaterialList
import com.kazurayam.materialstore.filesystem.Store
import com.kazurayam.materialstore.filesystem.Stores
import com.kazurayam.materialstore.metadata.QueryOnMetadata
import com.kazurayam.materialstore.metadata.SortKeys
import groovy.json.JsonOutput
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.regex.Pattern

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull

class ArtifactTest {

    private static Path outputDir =
            Paths.get(".").resolve("build/tmp/testOutput")
                    .resolve(ArtifactTest.class.getName())

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
    void test_getDescription_more() {
        QueryOnMetadata mp = QueryOnMetadata.builderWithMap([
                "URL.path": "/",
                "profile": "Flaskr_ProductionEnv",
                "step":"6"
        ]).build()
        SortKeys sortKeys =
                new SortKeys("step", "profile")
        Artifact artifact =
                new Artifact.Builder(
                        Material.NULL_OBJECT, Material.NULL_OBJECT, JobTimestamp.now())
                        .setQueryOnMetadata(mp)
                        .sortKeys(sortKeys)
                        .build()
        String description = artifact.getDescription()
        assertEquals('''{"step":"6", "profile":"Flaskr_ProductionEnv", "URL.path":"/"}''',
                description)
    }

    @Test
    void test_getDescription() {
        QueryOnMetadata mp = QueryOnMetadata.builderWithMap([
                "URL.host": "demoaut-mimic.kazurayam.com",
                "URL.file": "/"
        ]).build()
        Artifact artifact =
                new Artifact.Builder(
                        Material.NULL_OBJECT, Material.NULL_OBJECT, JobTimestamp.now())
                        .setQueryOnMetadata(mp)
                        .build()
        assertEquals(
                '''{"URL.file":"/", "URL.host":"demoaut-mimic.kazurayam.com"}''',
                artifact.getDescription())
    }

    @Test
    void test_toString() {
        QueryOnMetadata mp = QueryOnMetadata.builderWithMap([
                "URL.host": "demoaut-mimic.kazurayam.com",
                "URL.file": "/"
        ]).build()
        Artifact artifact =
                new Artifact.Builder(
                        Material.NULL_OBJECT, Material.NULL_OBJECT, JobTimestamp.now())
                        .setQueryOnMetadata(mp)
                        .build()
        println JsonOutput.prettyPrint(artifact.toString())
    }

    @Test
    void test_toString_alt() {
        Path root = outputDir.resolve("store")
        Store store = Stores.newInstance(root)
        JobName jobName = new JobName("test_toString_alt")
        // stuff the Job directory with a fixture
        Path jobNameDir = root.resolve(jobName.toString())
        FileUtils.copyDirectory(resultsDir.toFile(), jobNameDir.toFile())
        //
        Jobber jobberOfLeft = store.getJobber(jobName,
                new JobTimestamp("20210715_145922"))
        MaterialList leftList = jobberOfLeft.selectMaterials(
                QueryOnMetadata.builder()
                        .put("profile", "ProductionEnv")
                        .put("URL.file", Pattern.compile(".*"))
                        .build(),
                FileType.PNG)
        assert 2 == leftList.size()
        //
        Jobber jobberOfRight = store.getJobber(jobName,
                new JobTimestamp("20210715_145922"))
        MaterialList rightList= jobberOfRight.selectMaterials(
                QueryOnMetadata.builder()
                        .put("profile", "DevelopmentEnv")
                        .put("URL.file", Pattern.compile(".*"))
                        .build(),
                FileType.PNG)
        assert 2 == rightList.size()
        //
        ArtifactGroup artifactGroup =
                ArtifactGroup.builder(leftList, rightList)
                        .ignoreKeys("profile", "URL", "URL.host", "category")
                        .build()
        assertNotNull(artifactGroup)

        println JsonOutput.prettyPrint(artifactGroup.toString())

        assert 2 == artifactGroup.size()
        //
        println artifactGroup.get(0).toString()
    }

}
