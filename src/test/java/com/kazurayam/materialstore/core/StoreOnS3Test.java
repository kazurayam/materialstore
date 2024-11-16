package com.kazurayam.materialstore.core;

import com.kazurayam.materialstore.zest.TestOutputOrganizerFactory;
import com.kazurayam.timekeeper.Measurement;
import com.kazurayam.timekeeper.Table;
import com.kazurayam.timekeeper.Timekeeper;
import com.kazurayam.unittest.DeleteDir;
import com.kazurayam.unittest.TestOutputOrganizer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// this test may take long time because remote I/O to AWS S3 is slow,
// so usually this test should be disabled for faster test run
@Disabled
public class StoreOnS3Test {

    private static final TestOutputOrganizer too = TestOutputOrganizerFactory.create(StoreOnS3Test.class);
    private static final Logger logger = LoggerFactory.getLogger(StoreOnS3Test.class);
    private static final String BUCKET_NAME = "/com.kazurayam.materialstore.core.filesystem.store-on-s3-test";
    private static FileSystem s3fs;
    private static final boolean CLEANUP_ON_END = false;
    private static Timekeeper tk;
    private static Measurement mm;
    private Path dir;

    @BeforeAll
    public static void beforeAll() throws URISyntaxException, IOException {
        tk = new Timekeeper();
        mm = new Measurement.Builder("s3fs performance analysis",
                Collections.singletonList("Step")).build();
        tk.add(new Table.Builder(mm).build());
        //
        LocalDateTime beforeNewFileSystem = LocalDateTime.now();
        s3fs = FileSystems.newFileSystem(new URI("s3:///s3.ap-northeast-1.AMAZONAWS.COM/"),
                new HashMap<String, Object>(),
                Thread.currentThread().getContextClassLoader());
        assertNotNull(s3fs);
        LocalDateTime afterNewFileSystem = LocalDateTime.now();
        mm.recordDuration(Collections.singletonMap("Step", "creating new FileSystem on S3"),
                beforeNewFileSystem, afterNewFileSystem);
    }


    @AfterEach
    public void afterEach() throws IOException {
        if (CLEANUP_ON_END) {
            if (Files.exists(dir)) {
                DeleteDir.deleteDirectoryRecursively(dir);
            }
        }
    }

    @AfterAll
    public static void afterAll() throws IOException {
        LocalDateTime beforeClosing = LocalDateTime.now();
        s3fs.close();
        LocalDateTime afterClosing = LocalDateTime.now();
        mm.recordDuration(Collections.singletonMap("Step", "closing the FileSystem"),
                beforeClosing, afterClosing);
        // write the performance report
        Path markdown = too.cleanClassOutputDirectory().resolve("performance.md");
        tk.report(markdown);
    }

    @Test
    public void testS3fs() throws URISyntaxException, IOException {
        // create a directory in a S3 bucket if the directory is not present
        LocalDateTime beforeCreatingParentDir = LocalDateTime.now();
        dir = s3fs.getPath(BUCKET_NAME, "testS3fs");
        LocalDateTime afterCreatingParentDir = LocalDateTime.now();
        mm.recordDuration(Collections.singletonMap("Step", "creating parent dir"),
                beforeCreatingParentDir, afterCreatingParentDir);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        assertTrue(Files.exists(dir));

        // create a file in the dir
        LocalDateTime beforeWritingFile = LocalDateTime.now();
        Path file = dir.resolve("hello.txt");
        Files.write(file, "Hello, s3fs world!".getBytes());
        LocalDateTime afterWritingFile = LocalDateTime.now();
        mm.recordDuration(Collections.singletonMap("Step", "writing a file"),
                beforeWritingFile, afterWritingFile);
        assertTrue(Files.exists(file));

        // list a directory
        LocalDateTime beforeListingDir = LocalDateTime.now();
        assertEquals(1, Files.list(dir).count());
        LocalDateTime afterListingDir = LocalDateTime.now();
        mm.recordDuration(Collections.singletonMap("Step", "listing a dir"),
                beforeListingDir, afterListingDir);

        // delete the file
        LocalDateTime beforeDeletingFile = LocalDateTime.now();
        Files.delete(file);
        LocalDateTime afterDeletingFile = LocalDateTime.now();
        mm.recordDuration(Collections.singletonMap("Step", "deleting a file"),
                beforeDeletingFile, afterDeletingFile);
        assertEquals(0, Files.list(dir).count());

        // delete the dir
        LocalDateTime beforeDeletingDir = LocalDateTime.now();
        Files.delete(dir);
        LocalDateTime afterDeletingDir = LocalDateTime.now();
        mm.recordDuration(Collections.singletonMap("Step", "deleting a dir"),
                beforeDeletingDir, afterDeletingDir);
        assertFalse(Files.exists(dir));
    }

    @Test
    public void testOperateStoreOnS3() throws IOException, MaterialstoreException, JobNameNotFoundException {
        // create a directory in a S3 bucket if the directory is not present
        dir = s3fs.getPath(BUCKET_NAME, "testOperateStoreOnS3");
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        assertTrue(Files.exists(dir));
        Path root = dir.resolve("store");
        Files.createDirectories(root);
        // create the store
        Store store = Stores.newInstance(root);

        // write a material into the store
        JobName jobName = new JobName("foo");
        JobTimestamp jobTimestamp = JobTimestamp.now();
        Metadata metadata = Metadata.NULL_OBJECT;
        Material material =
                store.write(jobName, jobTimestamp, FileType.TXT, metadata, "Hello, world!");
        assertTrue(Files.exists(material.toPath()));

        // select a MaterialList out of the store
        MaterialList materialList = store.select(jobName, jobTimestamp);
        assertEquals(1, materialList.size());

        // copy materials from a JobTimestamp directory to another
        JobTimestamp jobTimestamp2 = JobTimestamp.laterThan(jobTimestamp);
        store.copyMaterials(jobName, jobTimestamp, jobTimestamp2);
        MaterialList copyList = store.select(jobName, jobTimestamp2);
        assertEquals(1, copyList.size());

        // delete the JobTimestamp directory and the materials from the copied directory
        store.deleteJobTimestamp(jobName, jobTimestamp2);

        // delete the JobName directory
        //store.deleteJobName(jobName);
        // the above line will fail because no empty directory can be present on S3 bucket.
    }
}
