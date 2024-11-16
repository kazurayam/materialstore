package com.kazurayam.materialstore.core;

import com.kazurayam.materialstore.zest.FixtureDirectory;
import com.kazurayam.materialstore.zest.SampleFixtureInjector;
import com.kazurayam.materialstore.zest.TestOutputOrganizerFactory;
import com.kazurayam.unittest.TestOutputOrganizer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StoreTest {

    private static final Logger log = LoggerFactory.getLogger(StoreTest.class);

    private static final TestOutputOrganizer too = TestOutputOrganizerFactory.create(StoreTest.class);

    @BeforeAll
    public static void beforeAll() throws IOException {
        too.cleanClassOutputDirectory();
    }

    @BeforeEach
    public void beforeEach() throws IOException {}

    @Test
    public void test_contains_JobName() throws Exception {
        Path methodDir = too.resolveMethodOutputDirectory("test_contains_JobName");
        Store store = Stores.newInstance(methodDir.resolve("store"));
        JobName jobName = new JobName("test_contains_JobName");
        JobTimestamp jtA = SampleFixtureInjector.create3TXTs(store, jobName, JobTimestamp.now());
        assertTrue(store.contains(jobName));
    }

    @Test
    public void test_findAllJobNames() throws Exception {
        Path methodDir = too.resolveMethodOutputDirectory("test_findAllJobNames");
        Store store = Stores.newInstance(methodDir.resolve("store"));
        JobName jobName = new JobName("test_findAllJobNames");
        JobTimestamp jtA = SampleFixtureInjector.create3TXTs(store, jobName, JobTimestamp.now());
        assertTrue(!store.findAllJobNames().isEmpty());
        assertTrue(store.findAllJobNames().contains(jobName));
    }

    @Test
    public void test_findDifferentiatingJobTimestamps() throws Exception {
        Path methodDir = too.resolveMethodOutputDirectory("test_findDifferentiatingJobTimestamps");
        FixtureDirectory fixtureDir = new FixtureDirectory("issue#331");
        fixtureDir.copyInto(methodDir);
        Store store = Stores.newInstance(methodDir.resolve("store"));
        JobName jobName = new JobName("CURA");
        assertTrue(store.contains(jobName),
                String.format("JobName \"%s\" is not found", jobName));
        //
        List<JobTimestamp> diffJobTimestamps = store.findDifferentiatingJobTimestamps(jobName);
        assertEquals(2, diffJobTimestamps.size());
    }

    @Test
    public void test_deleteJobName() throws Exception {
        Path methodDir = too.resolveMethodOutputDirectory("test_deleteJobName");
        Store store = Stores.newInstance(methodDir.resolve("store"));
        JobName jobName = new JobName("test_deleteJobName");
        JobTimestamp jtA = SampleFixtureInjector.create3TXTs(store, jobName, JobTimestamp.now());
        assertTrue(store.contains(jobName));
        int i = store.deleteJobName(jobName);
        assertFalse(store.contains(jobName));
    }

    @Test
    public void test_export() throws Exception {
        Path methodDir = too.resolveMethodOutputDirectory("test_export");
        Store store = Stores.newInstance(methodDir.resolve("store"));
        JobName jobName = new JobName("test_export");
        JobTimestamp jobTimestamp =
                SampleFixtureInjector.create3TXTs(store, jobName, JobTimestamp.now());
        QueryOnMetadata query = QueryOnMetadata.builder()
                .put("label", "it is red").build();
        Material m = store.selectSingle(jobName, jobTimestamp, query);
        assertNotNull(m);
        Path target = methodDir.resolve("exported.txt");
        store.export(m, target);
        assertTrue(Files.exists(target));
    }

    @Test
    public void test_findNthJobTimestamp_normal() throws Exception {
        Path methodDir = too.resolveMethodOutputDirectory("test_findNthJobTimestamp_normal");
        Store store = Stores.newInstance(methodDir.resolve("store"));
        JobName jobName = new JobName("test_findNthJobTimestamp_normal");
        JobTimestamp jtA = SampleFixtureInjector.create3TXTs(store, jobName, JobTimestamp.now());
        JobTimestamp jtB = SampleFixtureInjector.create3TXTs(store, jobName, JobTimestamp.laterThan(jtA)); // intentionally create 2 JobTimestamps
        List<JobTimestamp> jobTimestampList = store.findAllJobTimestamps(jobName);
        assertTrue(jobTimestampList.size() >= 2);
        // findNthJobTimestamps regards the list of JobTimestamp in the descending order
        assertEquals(jtB, store.findNthJobTimestamp(jobName, 1));
        assertEquals(jtA, store.findNthJobTimestamp(jobName, 2));
    }

    @Test
    public void test_findNthJobTimestamp_exceedingRange() throws Exception {
        Path methodDir = too.resolveMethodOutputDirectory("test_findNthJobTimestamp_exceedingRange");
        Store store = Stores.newInstance(methodDir.resolve("store"));
        JobName jobName = new JobName("test_findNthJobTimestamp_exceedingRange");
        JobTimestamp jtA = SampleFixtureInjector.create3PNGs(store, jobName, JobTimestamp.now());
        JobTimestamp jtB = SampleFixtureInjector.create3PNGs(store, jobName, JobTimestamp.laterThan(jtA)); // intentionally create 2 JobTimestamps
        List<JobTimestamp> jobTimestampList = store.findAllJobTimestamps(jobName);
        assertTrue(jobTimestampList.size() >= 2);
        // if nth parameter exceeds the range, return the last jobTimestamp
        assertEquals(jtB, store.findNthJobTimestamp(jobName, 999));
    }

    @Test
    public void test_getPathOf_JobName() throws Exception {
        Path methodDir = too.resolveMethodOutputDirectory("test_getPathOf_JobName");
        Store store = Stores.newInstance(methodDir.resolve("store"));
        JobName jobName = new JobName("test_getPathOf_JobName");
        JobTimestamp jtA = SampleFixtureInjector.create3TXTs(store, jobName, JobTimestamp.now());
        //
        Path jobNamePath = store.getPathOf(jobName);
        assertNotNull(jobNamePath);
        assertTrue(jobNamePath.toString().endsWith(jobName.toString()));
    }

    @Test
    public void test_getPathOf_JobTimestamp() throws Exception {
        Path methodDir = too.resolveMethodOutputDirectory("test_getPathOf_JobTimestamp");
        Store store = Stores.newInstance(methodDir.resolve("store"));
        JobName jobName = new JobName("test_getPathOf_JobTimestamp");
        JobTimestamp jobTimestamp = SampleFixtureInjector.create3TXTs(store, jobName, JobTimestamp.now());
        //
        Path jobTimestampPath = store.getPathOf(jobName, jobTimestamp);
        assertNotNull(jobTimestampPath);
        assertTrue(jobTimestampPath.toString().endsWith(jobTimestamp.toString()));
    }

    @Test
    public void test_reflect() throws Exception {
        Path methodDir = too.resolveMethodOutputDirectory("test_reflect");
        Store store = Stores.newInstance(methodDir.resolve("store"));
        JobName jobName = new JobName("test_reflect");
        JobTimestamp jt = JobTimestamp.now();
        store.write(jobName, jt, FileType.TXT, Metadata.NULL_OBJECT, "Hello");
        MaterialList ml = store.select(jobName, jt, FileType.PNG);   // ml.size() == 0
        // no MaterialstoreException to be thrown
        assertDoesNotThrow(() -> store.reflect(ml, jt.minusMinutes(3)));
        assertEquals(MaterialList.NULL_OBJECT, store.reflect(ml, jt.minusMinutes(3)));
    }

    @Test
    public void test_NULL_OBJECT() {
        Store obj = Store.NULL_OBJECT;
        assertNotNull(obj);
        assertTrue(Files.exists(obj.getRoot()));
        log.debug("[test_NULL_OBJECT] " + obj.toString());
    }

    @Test
    public void test_write_BufferedImage_as_JPEG() throws Exception {
        Path methodDir = too.resolveMethodOutputDirectory("test_write_BufferedImage_as_JPEG");
        Store store = Stores.newInstance(methodDir.resolve("store"));
        JobName jobName = new JobName("test_write_BufferedImage_as_JPEG");
        JobTimestamp jobTimestamp = SampleFixtureInjector.create3PNGs(store, jobName, JobTimestamp.now());
        QueryOnMetadata query = QueryOnMetadata.builder().put("step", "01").build();
        MaterialList materialList = store.select(jobName, jobTimestamp, FileType.PNG, query);
        assert materialList != null;
        //
        Material apple = materialList.get(0);
        assert apple != null;
        log.debug("apple.id=" + apple.getID());
        BufferedImage bufferedImage = ImageIO.read(apple.toPath().toFile());
        assert bufferedImage != null;
        assert bufferedImage.getHeight() > 0;
        assert bufferedImage.getWidth() > 0;
        //
        Metadata metadata = Metadata.builder().put("compressionQuality", "1.0").build();
        Material mt = store.write(jobName, jobTimestamp, FileType.JPG, metadata, bufferedImage);
        assert mt != null;
    }
}
