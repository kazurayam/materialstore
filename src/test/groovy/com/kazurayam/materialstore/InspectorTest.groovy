package com.kazurayam.materialstore

import com.kazurayam.materialstore.filesystem.*
import com.kazurayam.materialstore.reduce.MProductGroup
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.jupiter.api.Assertions.*

class InspectorTest {

    private static Path outputDir =
            Paths.get(".")
                    .resolve("build/tmp/testOutput")
                    .resolve(InspectorTest.class.getName())

    private static Path storeDir = outputDir.resolve("store")
    private static Path issue80Dir =
            Paths.get(".").resolve("src/test/fixture/issue#80")

    private Store store
    private JobName jobName
    private JobTimestamp timestampP
    private JobTimestamp timestampD
    private MaterialList left
    private MaterialList right
    private Inspector inspector

    @BeforeAll
    static void beforeAll() {
        if (Files.exists(outputDir)) {
            FileUtils.deleteDirectory(outputDir.toFile())
        }
        Files.createDirectories(outputDir)
        FileUtils.copyDirectory(issue80Dir.toFile(), storeDir.toFile())
    }

    @BeforeEach
    void before() {
        store = Stores.newInstance(storeDir)
        jobName = new JobName("MyAdmin_visual_inspection_twins")
        timestampP = new JobTimestamp("20220128_191320")
        left = store.select(jobName, timestampP,
                QueryOnMetadata.builder(["profile": "MyAdmin_ProductionEnv" ]).build()
        )
        timestampD = new JobTimestamp("20220128_191342")
        right = store.select(jobName, timestampD,
                QueryOnMetadata.builder(["profile": "MyAdmin_DevelopmentEnv" ]).build()
        )
        inspector = Inspector.newInstance(store)
    }

    @Test
    void test_apply() {
        MProductGroup prepared =
                MProductGroup.builder(left, right)
                        .ignoreKeys("profile", "URL.host", "URL.port", "URL.protocol")
                        .identifyWithRegex(["URL.query":"\\w{32}"])
                        .sort("URL.host")
                        .build()
        assertNotNull(prepared)

        MProductGroup stuffed = inspector.reduce(prepared)
        assertNotNull(stuffed)

        stuffed.each { mProduct ->
            //println JsonOutput.prettyPrint(mProduct.toString())
            assertNotEquals(ID.NULL_OBJECT, mProduct.getDiff().getIndexEntry().getID())
        }
        assertEquals(8, stuffed.size())
    }

    @Test
    void test_report_MaterialList() {
        JobName jobName = new JobName("MyAdmin_visual_inspection_twins")
        JobTimestamp jobTimestamp = new JobTimestamp("20220128_191320")
        MaterialList materialList = store.select(jobName, jobTimestamp, QueryOnMetadata.ANY)
        Inspector inspector = Inspector.newInstance(store)
        Path report = inspector.report(materialList, "test_reportMaterials.html")
        assertNotNull(report)
        assertTrue(Files.exists(report))
    }

    @Test
    void test_report_MProductGroup() {
        MProductGroup preparedAG =
                MProductGroup.builder(left, right)
                        .ignoreKeys("profile", "URL.host", "URL.port", "URL.protocol")
                        .identifyWithRegex(["URL.query":"\\w{32}"])
                        .sort("URL.host")
                        .build()
        MProductGroup reduced = inspector.reduce(preparedAG)
        double criteria = 0.0D
        assertTrue(reduced.countWarnings(criteria) > 0)

        JobName jobName = new JobName("MyAdmin_visual_inspection_twins")
        Path report = inspector.report(reduced, criteria,"test_report_MProductGroup.html")
        assertNotNull(report)
        assertTrue(Files.exists(report))
    }

}
