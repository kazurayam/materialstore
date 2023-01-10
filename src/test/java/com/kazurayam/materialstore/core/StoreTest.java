package com.kazurayam.materialstore.core;

import com.kazurayam.materialstore.TestFixtureSupport;
import com.kazurayam.materialstore.TestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    private Path testClassOutputDir;
    private Store store;
    private JobName jobName;

    @BeforeEach
    public void beforeEach() throws IOException {
        testClassOutputDir = TestHelper.createTestClassOutputDir(StoreTest.class);
        store = Stores.newInstance(testClassOutputDir.resolve("store"));
    }

    @Test
    public void test_contains_JobName() throws MaterialstoreException {
        jobName = new JobName("test_contains_JobName");
        JobTimestamp jtA = TestFixtureSupport.create3TXTs(store, jobName, JobTimestamp.now());
        assertTrue(store.contains(jobName));
    }

    @Test
    public void test_findAllJobNames() throws MaterialstoreException {
        jobName = new JobName("test_findAllJobNames");
        JobTimestamp jtA = TestFixtureSupport.create3TXTs(store, jobName, JobTimestamp.now());
        assertTrue(store.findAllJobNames().size() > 0);
        assertTrue(store.findAllJobNames().contains(jobName));
    }

    @Test
    public void test_findDifferentiatingJobTimestamps() throws MaterialstoreException, IOException {
        Path fixtureDir = TestHelper.getFixturesDirectory().resolve("issue#331");
        TestHelper.copyDirectory(fixtureDir, testClassOutputDir);
        JobName jobName = new JobName("CURA");
        assertTrue(store.contains(jobName),
                String.format("JobName \"%s\" is not found", jobName));
        //
        List<JobTimestamp> diffJobTimestamps = store.findDifferentiatingJobTimestamps(jobName);
        assertEquals(2, diffJobTimestamps.size());
    }

    @Test
    public void test_deleteJobName() throws MaterialstoreException {
        jobName = new JobName("test_deleteJobName");
        JobTimestamp jtA = TestFixtureSupport.create3TXTs(store, jobName, JobTimestamp.now());
        assertTrue(store.contains(jobName));
        //
        int i = store.deleteJobName(jobName);
        assertFalse(store.contains(jobName));
    }

    @Test
    public void test_findNthJobTimestamp_normal() throws MaterialstoreException {
        jobName = new JobName("test_findNthJobTimestamp_normal");
        JobTimestamp jtA = TestFixtureSupport.create3TXTs(store, jobName, JobTimestamp.now());
        JobTimestamp jtB = TestFixtureSupport.create3TXTs(store, jobName, JobTimestamp.laterThan(jtA)); // intentionally create 2 JobTimestamps
        List<JobTimestamp> jobTimestampList = store.findAllJobTimestamps(jobName);
        assertTrue(jobTimestampList.size() >= 2);
        // findNthJobTimestamps regards the list of JobTimestamp in the descending order
        assertEquals(jtB, store.findNthJobTimestamp(jobName, 1));
        assertEquals(jtA, store.findNthJobTimestamp(jobName, 2));
    }

    @Test
    public void test_findNthJobTimestamp_exceedingRange() throws MaterialstoreException {
        jobName = new JobName("test_findNthJobTimestamp_exceedingRange");
        JobTimestamp jtA = TestFixtureSupport.create3PNGs(store, jobName, JobTimestamp.now());
        JobTimestamp jtB = TestFixtureSupport.create3PNGs(store, jobName, JobTimestamp.laterThan(jtA)); // intentionally create 2 JobTimestamps
        List<JobTimestamp> jobTimestampList = store.findAllJobTimestamps(jobName);
        assertTrue(jobTimestampList.size() >= 2);
        // if nth parameter exceeds the range, return the last jobTimestamp
        assertEquals(jtB, store.findNthJobTimestamp(jobName, 999));
    }

    @Test
    public void test_getPathOf_JobName() throws MaterialstoreException {
        jobName = new JobName("test_getPathOf_JobName");
        JobTimestamp jtA = TestFixtureSupport.create3TXTs(store, jobName, JobTimestamp.now());
        //
        Path jobNamePath = store.getPathOf(jobName);
        assertNotNull(jobNamePath);
        assertTrue(jobNamePath.toString().endsWith(jobName.toString()));
    }

    @Test
    public void test_getPathOf_JobTimestamp() throws MaterialstoreException {
        jobName = new JobName("test_getPathOf_JobTimestamp");
        JobTimestamp jobTimestamp = TestFixtureSupport.create3TXTs(store, jobName, JobTimestamp.now());
        //
        Path jobTimestampPath = store.getPathOf(jobName, jobTimestamp);
        assertNotNull(jobTimestampPath);
        assertTrue(jobTimestampPath.toString().endsWith(jobTimestamp.toString()));
    }

    @Test
    public void test_reflect() throws MaterialstoreException {
        jobName = new JobName("test_reflect");
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
        //System.out.println(obj.toString());
    }
}
