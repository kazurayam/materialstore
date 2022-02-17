package com.kazurayam.materialstore

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

class DiffArtifactTest {

    private static Path outputDir =
            Paths.get(".").resolve("build/tmp/testOutput")
                    .resolve(DiffArtifactTest.class.getName())

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
    void test_getDescription_more() {
        MetadataPattern mp = MetadataPattern.builderWithMap([
                "URL.path": "/",
                "profile": "Flaskr_ProductionEnv",
                "step":"6"
        ]).build()
        DiffArtifactComparisonPriorities comparisonPriorities =
                new DiffArtifactComparisonPriorities("step", "profile")
        DiffArtifact diffArtifact =
                new DiffArtifact.Builder(
                        Material.NULL_OBJECT,
                        Material.NULL_OBJECT)
                        .setMetadataPattern(mp)
                        .setDiffArtifactComparisonPriorities(comparisonPriorities)
                        .build()
        String description = diffArtifact.getDescription()
        assertEquals('''{"step":"6", "profile":"Flaskr_ProductionEnv", "URL.path":"/"}''',
                description)
    }

    @Test
    void test_getDescription() {
        MetadataPattern mp = MetadataPattern.builderWithMap([
                "URL.host": "demoaut-mimic.kazurayam.com",
                "URL.file": "/"
        ]).build()
        DiffArtifact diffArtifact =
                new DiffArtifact.Builder(
                        Material.NULL_OBJECT,
                        Material.NULL_OBJECT)
                        .setMetadataPattern(mp)
                        .build()
        assertEquals(
                '''{"URL.file":"/", "URL.host":"demoaut-mimic.kazurayam.com"}''',
                diffArtifact.getDescription())
    }

    @Test
    void test_toString() {
        MetadataPattern mp = MetadataPattern.builderWithMap([
                "URL.host": "demoaut-mimic.kazurayam.com",
                "URL.file": "/"
        ]).build()
        DiffArtifact diffArtifact =
                new DiffArtifact.Builder(
                        Material.NULL_OBJECT,
                        Material.NULL_OBJECT)
                        .setMetadataPattern(mp)
                        .build()
        println JsonOutput.prettyPrint(diffArtifact.toString())
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
                MetadataPattern.builder()
                        .put("profile", "ProductionEnv")
                        .put("URL.file", Pattern.compile(".*"))
                        .build(),
                FileType.PNG)
        assert 2 == leftList.size()
        //
        Jobber jobberOfRight = store.getJobber(jobName,
                new JobTimestamp("20210715_145922"))
        MaterialList rightList= jobberOfRight.selectMaterials(
                MetadataPattern.builder()
                        .put("profile", "DevelopmentEnv")
                        .put("URL.file", Pattern.compile(".*"))
                        .build(),
                FileType.PNG)
        assert 2 == rightList.size()
        //
        DiffArtifacts diffArtifacts =
                store.zipMaterials(leftList, rightList,
                        IgnoringMetadataKeys.of("profile", "URL", "URL.host", "category"))
        assertNotNull(diffArtifacts)

        println JsonOutput.prettyPrint(diffArtifacts.toString())

        assert 2 == diffArtifacts.size()
        //
        println diffArtifacts.get(0).toString()
    }

}
