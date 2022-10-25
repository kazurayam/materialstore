package com.kazurayam.materialstore.manage;

import com.kazurayam.materialstore.TestCaseSupport;
import com.kazurayam.materialstore.TestHelper;
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

    private TestCaseSupport tcSupport;

    @BeforeEach
    public void beforeEach() throws IOException {
        tcSupport = new TestCaseSupport(this);
        store = tcSupport.getStore();
    }

    @Test
    public void test_deleteJobTimestampsOlderThan() throws MaterialstoreException, IOException {
        jobName = new JobName("test_deleteJobTimestampsOlderThan");
        JobTimestamp jtA = tcSupport.create3TXTsWithStepAndLabel(jobName, JobTimestamp.now());
        JobTimestamp jtB = tcSupport.create3TXTsWithStepAndLabel(jobName, JobTimestamp.laterThan(jtA)); // intentionally create 2 JobTimestamps
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
    public void test_cleanup() throws MaterialstoreException, IOException {
        // Arrange
        StoreCleaner cleaner = StoreCleaner.newInstance(store);
        //
        Path fixtureDir = TestHelper.getFixturesDirectory().resolve("issue#327");
        tcSupport.copyFixture(fixtureDir, tcSupport.getOutputDir());
        JobName jobName = new JobName("CURA");
        assertTrue(store.contains(jobName),
                String.format("JobName \"%s\" is not found", jobName));
        List<JobTimestamp> jobTimestampsBeforeCleanUp = store.findAllJobTimestamps(jobName);
        assertEquals(5, jobTimestampsBeforeCleanUp.size());
        List<Path> reportFilesBeforeCleanUp = cleaner.findAllReportsOf(jobName);
        assertEquals(2, reportFilesBeforeCleanUp.size());
        // Action
        cleaner.cleanup(jobName);
        // Assert
        List<JobTimestamp> jobTimestampsAfterCleanUp = store.findAllJobTimestamps(jobName);
        assertEquals(3, jobTimestampsAfterCleanUp.size());
        List<Path> reportFilesAfterCleanUp = cleaner.findAllReportsOf(jobName);
        assertEquals(1, reportFilesAfterCleanUp.size());
    }
}
