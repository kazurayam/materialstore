package com.kazurayam.materialstore.reduce

import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.JobTimestamp
import com.kazurayam.materialstore.filesystem.MaterialList
import com.kazurayam.materialstore.filesystem.Store
import com.kazurayam.materialstore.filesystem.Stores
import com.kazurayam.materialstore.metadata.QueryOnMetadata
import groovy.json.JsonOutput
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.function.BiFunction

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull

class MProductGroupBuilderTwinsTest {

    private static Path outputDir = Paths.get(".")
            .resolve("build/tmp/testOutput")
            .resolve(MProductGroupBuilderTwinsTest.class.getName())

    private static Path fixtureDir = Paths.get(".")
            .resolve("src/test/fixture/issue#80")

    private static Store store

    private JobName jobName
    private JobTimestamp timestampP
    private JobTimestamp timestampD

    MaterialList left
    MaterialList right
    MProductGroup mProductGroup

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
        jobName = new JobName("MyAdmin_visual_inspection_twins")
        timestampP = new JobTimestamp("20220128_191320")
        timestampD = new JobTimestamp("20220128_191342")
        left = store.select(jobName, timestampP,
                QueryOnMetadata.builder(["profile": "MyAdmin_ProductionEnv"]).build()
        )
        assert left.size() == 8
        right = store.select(jobName, timestampD,
                QueryOnMetadata.builder(["profile": "MyAdmin_DevelopmentEnv"]).build()
        )
        assert right.size() == 8
    }

    @Test
    void test_twins() {
        BiFunction<MaterialList, MaterialList, MProductGroup> func = {
            MaterialList left, MaterialList right ->
                MProductGroup.builder(left, right)
                    .ignoreKeys("profile", "URL.host")
                    .identifyWithRegex(["URL.query":"\\w{32}"])
                    .build()
        }
        MProductGroup reduced = MProductGroupBuilder.twins(store, left, right, func)
        assertNotNull(reduced)
        assertEquals(8, reduced.size())
        //println JsonOutput.prettyPrint(reduced.toString())
    }
}
