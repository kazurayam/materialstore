package com.kazurayam.materialstore.filesystem;

import com.kazurayam.materialstore.DuplicatingMaterialException;
import com.kazurayam.materialstore.MaterialstoreException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StoreTest {

    private static final Path outputDir = Paths.get(".").resolve("build/tmp/testOutput").resolve(StoreTest.class.getName());
    private static Store store;

    @BeforeAll
    public static void beforeAll() throws IOException {
        Path root = outputDir.resolve("store");
        if (Files.exists(root)) {
            FileUtils.deleteDirectory(root.toFile());
        }
        store = Stores.newInstance(root);
    }

    @Test
    public void test_write() throws MaterialstoreException {
        String caseName = "test_write";
        JobName jobName = new JobName(caseName);
        JobTimestamp jobTimestamp = JobTimestamp.now();
        Metadata metadata = Metadata.builder(
                Collections.singletonMap("case","1")).build();
        Material written = store.write(jobName, jobTimestamp, FileType.TXT, metadata, caseName);
        assertEquals("02206f5", written.getShortId());
    }

    @Test
    public void test_write_with_DuplicationHandling_TERMINATE() throws MaterialstoreException {
        String caseName = "test_write_with_DuplicationHandling_TERMINATE";
        JobName jobName = new JobName(caseName);
        JobTimestamp jobTimestamp = JobTimestamp.now();
        Metadata metadata = Metadata.builder(
                Collections.singletonMap("case","2")).build();
        store.write(jobName, jobTimestamp, FileType.TXT, metadata, caseName);
        DuplicatingMaterialException thrown =
                Assertions.assertThrows(DuplicatingMaterialException.class, () -> {
            store.write(jobName, jobTimestamp, FileType.TXT, metadata, caseName);
        });
        Assertions.assertTrue(thrown.getMessage().contains("is already there in the index"));
    }

    @Test
    public void test_write_with_DuplicationHandling_CONTINUE() throws MaterialstoreException {
        String caseName = "test_write_with_DuplicationHandling_CONTINUE";
        JobName jobName = new JobName(caseName);
        JobTimestamp jobTimestamp = JobTimestamp.now();
        Metadata metadata = Metadata.builder(
                Collections.singletonMap("case","3")).build();
        store.write(jobName, jobTimestamp, FileType.TXT, metadata, caseName);
        Material written = store.write(jobName, jobTimestamp, FileType.TXT, metadata, caseName,
                Jobber.DuplicationHandling.CONTINUE);
        assertEquals("31a4cf1", written.getShortId());
    }
}
