package com.kazurayam.materialstore.base.materialize;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class URLDownloaderTest {

    private static Path outputDir;

    @BeforeAll
    public static void beforeAll() throws IOException {
        Path projectDir = Paths.get(System.getProperty("user.dir"));
        outputDir = projectDir.resolve("build/tmp/testOutput").resolve(URLDownloaderTest.class.getName());
        Files.createDirectories(outputDir);
    }

    @BeforeEach
    public void setup() {
    }

    @Test
    public void test_download() throws IOException {
        URL url = new URL("https://www.data.jma.go.jp/rss/jma.rss");
        Path out = outputDir.resolve("jma.rss");
        URLDownloader.download(url, out);
        Assertions.assertTrue(Files.exists(out));
        Assertions.assertTrue(out.toFile().length() > 0);
    }

}
