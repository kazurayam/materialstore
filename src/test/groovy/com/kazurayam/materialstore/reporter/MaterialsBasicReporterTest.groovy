package com.kazurayam.materialstore.reporter


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
import java.util.regex.Pattern

import static org.junit.jupiter.api.Assertions.assertTrue

class MaterialsBasicReporterTest {

    private static Path outputDir =
            Paths.get(".").resolve("build/tmp/testOutput")
                    .resolve(MaterialsBasicReporterTest.class.getName())

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
    void test_report() {
        Path root = outputDir.resolve("store")
        Store store = new StoreImpl(root)
        JobName jobName = new JobName("test_report")
        // make sure the Job directory to be empty
        FileUtils.deleteDirectory(root.resolve(jobName.toString()).toFile())
        // stuff the Job directory with a fixture
        Path jobNameDir = root.resolve(jobName.toString())
        FileUtils.copyDirectory(resultsDir.toFile(), jobNameDir.toFile())
        //
        MaterialsBasicReporter reporter =
                new MaterialsBasicReporter(root, jobName)
        JobTimestamp jobTimestamp = new JobTimestamp("20210715_145922")
        MaterialList list = store.select(jobName, jobTimestamp,
                QueryOnMetadata.builder()
                        .put("profile", Pattern.compile(".*Env")).build()
        )
        assertTrue(list.size() > 0, "list is empty")
        String fileName = "list.html"
        Path report = reporter.reportMaterials(list, fileName)
        //
        assertTrue(Files.exists(report))
    }
}