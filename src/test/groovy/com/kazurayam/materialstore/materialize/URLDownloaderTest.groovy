package com.kazurayam.materialstore.materialize

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.jupiter.api.Assertions.assertTrue

class URLDownloaderTest {

    private static Path outputDir

    @BeforeAll
    static void beforeAll() {
        Path projectDir = Paths.get(System.getProperty("user.dir"))
        outputDir = projectDir.resolve("build/tmp/testOutput")
                .resolve(URLDownloaderTest.class.getName())
        Files.createDirectories(outputDir)
    }

    @BeforeEach
    void setup() {}

    @Test
    void test_download() {
        URL url = new URL("https://press.aboutamazon.com/rss/news-releases.xml")
        Path out = outputDir.resolve("AmznPress-rss.xml")
        URLDownloader.download(url, out)
        assertTrue(Files.exists(out))
        assertTrue(out.toFile().length() > 0)
    }
}
