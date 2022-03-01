package com.kazurayam.materialstore


import com.kazurayam.materialstore.filesystem.ID
import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.JobTimestamp
import com.kazurayam.materialstore.filesystem.MaterialList
import com.kazurayam.materialstore.filesystem.Store
import com.kazurayam.materialstore.filesystem.Stores
import com.kazurayam.materialstore.metadata.QueryOnMetadata
import com.kazurayam.materialstore.resolvent.MProductGroup
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.jupiter.api.Assertions.*

class MaterialstoreFacadeTest {

    private static Path outputDir =
            Paths.get(".")
                    .resolve("build/tmp/testOutput")
                    .resolve(MaterialstoreFacadeTest.class.getName())

    private static Path storeDir = outputDir.resolve("store")
    private static Path issue80Dir =
            Paths.get(".").resolve("src/test/fixture/issue#80")

    private Store store
    private JobName jobName
    private JobTimestamp timestampP
    private JobTimestamp timestampD
    private MaterialList left
    private MaterialList right
    private MaterialstoreFacade facade

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
        facade = MaterialstoreFacade.newInstance(store)
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

        MProductGroup stuffed = facade.reduce(prepared)
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
        MaterialstoreFacade facade = MaterialstoreFacade.newInstance(store)
        Path report = facade.report(jobName, materialList, "test_reportMaterials.html")
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
        MProductGroup reducedAG = facade.reduce(preparedAG)
        JobName jobName = new JobName("MyAdmin_visual_inspection_twins")
        double criteria = 0.0D
        Path report = facade.report(jobName, reducedAG, criteria,"test_report_MProductGroup.html")
        assertNotNull(report)
        assertTrue(Files.exists(report))
        assertTrue(reducedAG.countWarnings(criteria) > 0)
    }

}
