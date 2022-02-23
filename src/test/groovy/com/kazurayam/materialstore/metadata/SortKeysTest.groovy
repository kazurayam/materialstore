package com.kazurayam.materialstore.metadata


import com.kazurayam.materialstore.resolvent.ArtifactGroup
import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.JobTimestamp
import com.kazurayam.materialstore.filesystem.MaterialList
import com.kazurayam.materialstore.filesystem.Store
import com.kazurayam.materialstore.filesystem.Stores
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.jupiter.api.Assertions.*

class SortKeysTest {
    private static Path fixtureDir = Paths.get(".")
            .resolve("src/test/resources/fixture/issue#89")
    private static final Path outputDir = Paths.get(".")
            .resolve("build/tmp/testOutput")
            .resolve(SortKeysTest.class.getName())
    private static Store store

    private JobName jobName
    private JobTimestamp timestampP
    private JobTimestamp timestampD

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
    void setup() {
        jobName = new JobName("Flaskr_VisualInspectionTwins")
        timestampP = new JobTimestamp("20220217_103054")
        timestampD = new JobTimestamp("20220217_103106")

        left = store.select(jobName, timestampP,
                QueryOnMetadata.builderWithMap(["profile": "Flaskr_ProductionEnv"]).build()
        )
        assert left.size() == 14
        right = store.select(jobName, timestampD,
                QueryOnMetadata.builderWithMap(["profile": "Flaskr_DevelopmentEnv"]).build()
        )
        assert right.size() == 14
    }

    @Test
    void test_smoke() {
        ArtifactGroup das =
                ArtifactGroup.builder(left, right)
                        .ignoreKeys("profile", "URL.host", "URL.port")
                        .sort("step", "URL.path")
                        .build()
        assertEquals(14, das.size())
        das.each {it ->
            println it.getDescription()
        }
        assertTrue(das.get(0).getDescription().startsWith("{\"step\":\"1\""))
        assertTrue(das.get(1).getDescription().startsWith("{\"step\":\"1\""))
        assertTrue(das.get(2).getDescription().startsWith("{\"step\":\"2\""))
        assertTrue(das.get(3).getDescription().startsWith("{\"step\":\"2\""))
        assertTrue(das.get(4).getDescription().startsWith("{\"step\":\"3\""))
        assertTrue(das.get(5).getDescription().startsWith("{\"step\":\"3\""))
    }

    @Test
    void test_constructor() {
        assertNotNull(new SortKeys("step", "URL.path"))
    }

    @Test
    void test_toString() {
        String json = new SortKeys("step", "URL.path").toString()
        println json
    }

}
