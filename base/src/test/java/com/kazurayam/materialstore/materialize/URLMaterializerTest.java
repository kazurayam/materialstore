package com.kazurayam.materialstore.materialize;

import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.FileType;
import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.filesystem.Stores;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class URLMaterializerTest {

    private static Store store;

    @BeforeAll
    public static void beforeAll() throws IOException {
        Path projectDir = Paths.get(System.getProperty("user.dir"));
        Path outputDir = projectDir.resolve("build/tmp/testOutput").resolve(URLDownloaderTest.class.getName());
        Files.createDirectories(outputDir);
        Path root = outputDir.resolve("root");
        store = Stores.newInstance(root);
    }

    @BeforeEach
    public void setup() {
    }

    @Test
    public void test_materialize() throws IOException, MaterialstoreException {
        URL url = new URL("https://press.aboutamazon.com/rss/news-releases.xml");
        JobName jobName = new JobName("test_materialize");
        JobTimestamp jobTimestamp = JobTimestamp.now();
        FileType fileType = FileType.XML;
        URLMaterializer materializer = new URLMaterializer(store);
        Material material = materializer.materialize(url, jobName, jobTimestamp, fileType);
        Assertions.assertNotNull(material);
        Path file = material.toPath(store.getRoot());
        Assertions.assertTrue(Files.exists(file));
        Assertions.assertTrue(file.toFile().length() > 0);
    }


}
