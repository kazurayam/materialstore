package com.kazurayam.materialstore.reduce

import com.kazurayam.materialstore.filesystem.*
import com.kazurayam.materialstore.metadata.IdentifyMetadataValues
import com.kazurayam.materialstore.metadata.IgnoreMetadataKeys
import com.kazurayam.materialstore.metadata.Metadata
import com.kazurayam.materialstore.metadata.QueryOnMetadata
import com.kazurayam.materialstore.metadata.SortKeys
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

class MProductGroupBuilderChronosTest {

    private static Path outputDir = Paths.get(".")
            .resolve("build/tmp/testOutput")
            .resolve(MProductGroupBuilderChronosTest.class.getName())

    private static Path fixtureDir = Paths.get(".")
            .resolve("src/test/fixture/issue#80")

    private static boolean verbose = true   // set SLF4J log level to debug
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
        //
        if (verbose) {
            System.setProperty("org.slf4j.simpleLogger.log.com.kazurayam.materialstore.reduce.MProductGroup", "DEBUG")
        }
    }

    @BeforeEach
    void setup() {
        jobName = new JobName("MyAdmin_visual_inspection_twins")
        timestampD = new JobTimestamp("20220128_191342")
        right = store.select(jobName, timestampD,
                QueryOnMetadata.builder(["profile": "MyAdmin_DevelopmentEnv"]).build()
        )
        assert right.size() == 8
        assert right.countMaterialsWithIdStartingWith("5d7e467") == 1


        // modify the test fixture to meet the requirement for Chronos mode test
        // copy "20220128_191342" to "20220101_010101"; these 2 JobTimestamps makes a Chronos pair
        JobTimestamp timestampW = new JobTimestamp("20220101_010101")
        store.copyMaterials(jobName, timestampD, timestampW)

        left = store.select(jobName, timestampW,
                QueryOnMetadata.builder(["profile": "MyAdmin_DevelopmentEnv"]).build()
        )
        assert left.size() == 8
        assert left.countMaterialsWithIdStartingWith("5d7e467") == 1

        // modify the test fixture once more to reproduce the issue#167
        // https://github.com/kazurayam/materialstore/issues/167
        // Add one file into the previous JobTimestamp directory,
        // and do not add it into the current JobTimestamp directory
        // so that the 2 JobTimestamp directories to be asymmetric

        //store.write(jobName, timestampW, FileType.TXT, Metadata.NULL_OBJECT, "I am a annoyance".getBytes())
    }

    @Test
    void test_chronos() {
        BiFunction<MaterialList, MaterialList, MProductGroup> func = {
            MaterialList left, MaterialList right ->
                MProductGroup.builder(left, right)
                    .ignoreKeys("profile", "URL.host")
                    .identifyWithRegex(["URL.query":"\\w{32}"])
                    .build()
        }
        MProductGroup reduced = MProductGroupBuilder.chronos(store, right, func)
        assertNotNull(reduced)
        assertEquals(1, reduced.getMaterialListPrevious().countMaterialsWithIdStartingWith("5d7e467"))
        assertEquals(1, reduced.getMaterialListFollowing().countMaterialsWithIdStartingWith("5d7e467"))
        assertEquals(8, reduced.size())
    }
}
