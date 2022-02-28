package com.kazurayam.materialstore.map


import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.JobTimestamp
import com.kazurayam.materialstore.filesystem.Material
import com.kazurayam.materialstore.filesystem.MaterialList
import com.kazurayam.materialstore.filesystem.Store
import com.kazurayam.materialstore.filesystem.Stores
import com.kazurayam.materialstore.metadata.QueryOnMetadata
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

class IdentityMapperTest {

    private static Path outputDir =
            Paths.get(".").resolve("build/tmp/testOutput")
                    .resolve(IdentityMapperTest.class.getName())

    private static Path resultsDir =
            Paths.get(".").resolve("src/test/resources/fixture/sample_results")

    private Path root
    private Store store
    private JobName jobName

    @BeforeAll
    static void beforeAll() {
        if (Files.exists(outputDir)) {
            FileUtils.deleteDirectory(outputDir.toFile())
        }
        Files.createDirectories(outputDir)
    }

    @BeforeEach
    void beforeEach() {
        jobName = new JobName("IdentityMapperTest")
        root = outputDir.resolve("store")
        store = Stores.newInstance(root)
        Path target = root.resolve(jobName.toString())
        FileUtils.copyDirectory(resultsDir.toFile(), target.toFile())
    }

    @Test
    void test_smoke() {
        JobTimestamp fixtureTimestamp = new JobTimestamp("20210713_093357")
        QueryOnMetadata query = QueryOnMetadata.builderWithMap(["URL.host": "www.google.com"]).build()
        MaterialList mList = store.select(jobName, fixtureTimestamp, query)
        assertEquals(1, mList.size())
        Material source = mList.get(0)
        //
        Mapper mapper = new IdentityMapper()
        mapper.setStore(store)
        JobTimestamp jobTimestamp = JobTimestamp.now()
        MappedResultSerializer serializer = new MappedResultSerializer(store, jobName, jobTimestamp)
        mapper.setMappingListener(serializer)
        mapper.map(source)
        MaterialList serialized = store.select(jobName, jobTimestamp,
                QueryOnMetadata.builder().build())
        assertTrue(serialized.size() > 0)
        Material material = serialized.get(0)
        assertEquals("www.google.com", material.getMetadata().get("URL.host"))
    }
}
