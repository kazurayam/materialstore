package com.kazurayam.materialstore.manage;

import com.kazurayam.materialstore.TestCaseSupport;
import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StoreCleanerTest {

    private Store store;
    private JobName jobName;

    @BeforeEach
    public void beforeEach() throws IOException {
        store = TestCaseSupport.initializeStore(this);
    }

    @Test
    public void test_deleteJobTimestampsOlderThan() throws MaterialstoreException, IOException {
        jobName = new JobName("test_deleteJobTimestampsOlderThan");
        JobTimestamp jtA = TestCaseSupport.createFixtures(store, jobName, JobTimestamp.now());
        JobTimestamp jtB = TestCaseSupport.createFixtures(store, jobName, jtA); // intentionally create 2 JobTimestamps
        List<JobTimestamp> jobTimestampList = store.findAllJobTimestamps(jobName);
        assertTrue(jobTimestampList.size() >= 2);
        // delete JobTimestamp directories older than the one last 1 JobTimestamp.
        StoreCleaner scavenger = StoreCleaner.newInstance(store);
        int deleted = scavenger.deleteJobTimestampsOlderThan(jobName, store.findNthJobTimestamp(jobName, 1));
        assertTrue(deleted >= 1);
        assertEquals(1, store.findAllJobTimestamps(jobName).size());
    }

    @Test
    public void test_deleteReportsOlderThan() throws MaterialstoreException, IOException {
        jobName = new JobName("test_deleteReportsOlderThan");
        JobTimestamp jtA = JobTimestamp.now();
        JobTimestamp jtB = JobTimestamp.laterThan(jtA);
        // create the 2 reports
        Path reportA = store.getRoot().resolve(store.resolveReportFileName(jobName, jtA));
        Files.write(reportA, "<html><head><title>reportA</title></head></html>".getBytes());
        Path reportB = store.getRoot().resolve(store.resolveReportFileName(jobName, jtB));
        Files.write(reportB, "<html><head><title>reportB</title></head></html>".getBytes());
        // delete <JobName>-<JobTimestamp>.html files older than the one last 1 JobTimestamp
        StoreCleaner scavenger = StoreCleaner.newInstance(store);
        int deleted = scavenger.deleteReportsOlderThan(jobName, jtB);
        assertTrue(deleted >= 1);
        assertEquals(1,scavenger.findAllReportsOf(jobName).size());
    }

    @Test
    public void test_cleanup() throws IOException {
        jobName = new JobName("test_cleanup");
        Path sourceDir = TestCaseSupport.getFixtureDirectory("issue#327").resolve("store");
        Files.walk(sourceDir).forEach(source -> {

        });
    }
}
