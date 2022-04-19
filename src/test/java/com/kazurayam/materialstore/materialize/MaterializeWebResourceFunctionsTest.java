package com.kazurayam.materialstore.materialize;

import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.FileType;
import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.QueryOnMetadata;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.filesystem.Stores;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MaterializeWebResourceFunctionsTest {

    private static Path outputDir =
            Paths.get(System.getProperty("user.dir"))
                    .resolve("build/tmp/testOutput")
                    .resolve(MaterializingPageFunctionsTest.class.getName());

    private static Store store;

    @BeforeAll
    public static void beforeAll() throws IOException {
        if (Files.exists(outputDir)) {
            FileUtils.deleteDirectory(outputDir.toFile());
        }
        Files.createDirectories(outputDir);
        Path root = outputDir.resolve("store");
        store = Stores.newInstance(root);
    }

    @Test
    public void test_storeWebResource_jpg() throws MaterialstoreException {
        Target target = new Target.Builder("http://myadmin.kazurayam.com/umineko-1960x1960.jpg").build();
        JobName jobName = new JobName("test_storeWebResource_jpg");
        JobTimestamp jobTimestamp = JobTimestamp.now();
        StorageDirectory storageDirectory = new StorageDirectory(store, jobName, jobTimestamp);
        MaterializingWebResourceFunctions.storeWebResource.accept(target, storageDirectory);
        //
        Material material = store.selectSingle(jobName, jobTimestamp, FileType.JPG, QueryOnMetadata.ANY);
        assertNotNull(Files.exists(material.toPath(store.getRoot())));
    }
}
