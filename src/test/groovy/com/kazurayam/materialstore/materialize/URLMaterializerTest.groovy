package com.kazurayam.materialstore.materialize

import com.kazurayam.materialstore.filesystem.FileType
import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.JobTimestamp
import com.kazurayam.materialstore.filesystem.Material
import com.kazurayam.materialstore.filesystem.Store
import com.kazurayam.materialstore.filesystem.Stores
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertTrue

class URLMaterializerTest {

    private static Path outputDir
    private static Store store

    @BeforeAll
    static void beforeAll() {
        Path projectDir = Paths.get(System.getProperty("user.dir"))
        outputDir = projectDir.resolve("build/tmp/testOutput")
                .resolve(URLDownloaderTest.class.getName())
        Files.createDirectories(outputDir)
        Path root = outputDir.resolve("root")
        store = Stores.newInstance(root)
    }

    @BeforeEach
    void setup() {}

    @Test
    void test_materialize() {
        URL url = new URL("https://press.aboutamazon.com/rss/news-releases.xml")
        JobName jobName = new JobName("test_materialize")
        JobTimestamp jobTimestamp = JobTimestamp.now()
        FileType fileType = FileType.XML
        URLMaterializer materializer = new URLMaterializer(store)
        Material material = materializer.materialize(url, jobName, jobTimestamp, fileType)
        assertNotNull(material)
        Path file = material.toPath(store.getRoot())
        assertTrue(Files.exists(file))
        assertTrue(file.toFile().length() > 0)
    }
}
