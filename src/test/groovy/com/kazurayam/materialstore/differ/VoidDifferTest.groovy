package com.kazurayam.materialstore.differ

import com.kazurayam.materialstore.filesystem.FileType
import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.JobTimestamp
import com.kazurayam.materialstore.filesystem.Material
import com.kazurayam.materialstore.filesystem.MaterialList
import com.kazurayam.materialstore.filesystem.Store
import com.kazurayam.materialstore.filesystem.Stores
import com.kazurayam.materialstore.metadata.QueryOnMetadata
import com.kazurayam.materialstore.resolvent.MProductGroup
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.apache.commons.io.FileUtils

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull

class VoidDifferTest {

    private static Path outputDir
    private static Store store

    JobName jobName
    MProductGroup prepared

    @BeforeAll
    static void beforeAll() {
        Path projectDir = Paths.get(System.getProperty("user.dir"))
        outputDir = projectDir.resolve("build/tmp/testOutput")
                .resolve(VoidDifferTest.class.getName())
        if (Files.exists(outputDir)) {
            Files.walk(outputDir)
                    .sorted(Comparator.reverseOrder())
                    .map({p -> p.toFile()})
                    .forEach({f -> f.delete()});
        }
        Files.createDirectories(outputDir)
        Path fixtureDir = projectDir.resolve("src/test/fixture")
        FileUtils.copyDirectory(fixtureDir.resolve("issue#80").toFile(),
                outputDir.resolve("store").toFile())
        Path root = outputDir.resolve("store")
        store = Stores.newInstance(root)
    }

    @BeforeEach
    void setup() {
        jobName = new JobName("MyAdmin_visual_inspection_twins")
        JobTimestamp timestamp1 = new JobTimestamp("20220128_191320")
        JobTimestamp timestamp2 = new JobTimestamp("20220128_191342")
        MaterialList left = store.select(jobName, timestamp1,
                QueryOnMetadata.builder(["URL.path": "/npm/bootstrap-icons@1.5.0/font/fonts/bootstrap-icons.woff2"]).build(),
                FileType.WOFF2)
        MaterialList right = store.select(jobName, timestamp2,
                QueryOnMetadata.builder(["URL.path": "/npm/bootstrap-icons@1.7.2/font/fonts/bootstrap-icons.woff2"]).build(),
                FileType.WOFF2)
        prepared =
                MProductGroup.builder(left, right)
                        .ignoreKeys("profile", "URL.query")
                        .build()

        assertNotNull(prepared)
        assertEquals(1, prepared.size())
    }

    @Test
    void test_smoke() {
        VoidDiffer voidDiffer = new VoidDiffer()
        DifferDriver differDriver =
                new DifferDriverImpl.Builder(store)
                        .differFor(FileType.WOFF2, voidDiffer)
                        .build()
        differDriver.differentiate(prepared)
        //
        JobTimestamp latestTimestamp = store.findLatestJobTimestamp(jobName)
        MaterialList materialList = store.select(jobName, latestTimestamp, QueryOnMetadata.ANY)
        assertEquals(1, materialList.size())
        //
        Material material = materialList.get(0)
        assertEquals(FileType.HTML, material.getIndexEntry().getFileType())
    }
}
