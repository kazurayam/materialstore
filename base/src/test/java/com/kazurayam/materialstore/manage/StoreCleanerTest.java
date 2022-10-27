package com.kazurayam.materialstore.manage;

import com.kazurayam.materialstore.FixtureDirCopier;
import com.kazurayam.materialstore.TestFixtureSupport;
import com.kazurayam.materialstore.TestHelper;
import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.filesystem.Stores;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StoreCleanerTest {

    private Path testClassOutputDir;

    @BeforeEach
    public void beforeEach() throws IOException {
        testClassOutputDir = TestHelper.createTestClassOutputDir(this);
    }

    @Test
    public void test_deleteJobTimestampsOlderThan() throws MaterialstoreException, IOException {
        JobName jobName = new JobName("test_deleteJobTimestampsOlderThan");
        Path testCaseDir = testClassOutputDir.resolve(jobName.toString());
        Store store = Stores.newInstance(testCaseDir.resolve("store"));
        JobTimestamp jtA = TestFixtureSupport.create3TXTs(store, jobName, JobTimestamp.now());
        JobTimestamp jtB = TestFixtureSupport.create3TXTs(store, jobName, JobTimestamp.laterThan(jtA)); // intentionally create 2 JobTimestamps
        List<JobTimestamp> jobTimestampList = store.findAllJobTimestamps(jobName);
        assertTrue(jobTimestampList.size() >= 2);
        // delete JobTimestamp directories older than the one last 1 JobTimestamp.
        StoreCleaner cleaner = StoreCleaner.newInstance(store);
        int deleted = cleaner.deleteJobTimestampsOlderThan(jobName, store.findNthJobTimestamp(jobName, 1));
        assertTrue(deleted >= 1);
        assertEquals(1, store.findAllJobTimestamps(jobName).size());
    }

    @Test
    public void test_deleteReportsOlderThan() throws IOException, MaterialstoreException {
        JobName jobName = new JobName("test_deleteReportsOlderThan");
        Path testCaseDir = testClassOutputDir.resolve(jobName.toString());
        Store store = Stores.newInstance(testCaseDir.resolve("store"));
        JobTimestamp jtA = JobTimestamp.now();
        JobTimestamp jtB = JobTimestamp.laterThan(jtA);
        // create the 2 reports
        Path reportA = store.getRoot().resolve(store.resolveReportFileName(jobName, jtA));
        Files.write(reportA, "<html><head><title>reportA</title></head></html>".getBytes());
        Path reportB = store.getRoot().resolve(store.resolveReportFileName(jobName, jtB));
        Files.write(reportB, "<html><head><title>reportB</title></head></html>".getBytes());
        // delete <JobName>-<JobTimestamp>.html files older than the one last 1 JobTimestamp
        StoreCleaner cleaner = StoreCleaner.newInstance(store);
        int deleted = cleaner.deleteReportsOlderThan(jobName, jtB);
        assertTrue(deleted >= 1);
        assertEquals(1, cleaner.findAllReportsOf(jobName).size());
    }


    /**
     * make sure cleanup(JobName) without 2nd argument
     * will retain the latest unit of artifacts
     */
    @Test
    public void test_cleanup_with_diff() throws IOException, MaterialstoreException {
        String testCaseName = "test_cleanup_with_diff";
        Store store = FixtureDirCopier.copyIssue334FixtureInto(
                testClassOutputDir.resolve(testCaseName));
        StoreCleaner cleaner = StoreCleaner.newInstance(store);
        // Action
        JobName jobName = new JobName("CURA");
        cleaner.cleanup(jobName);
        // Assert
        List<JobTimestamp> jobTimestampsAfterCleanUp = store.findAllJobTimestamps(jobName);
        assertEquals(3, jobTimestampsAfterCleanUp.size());
        List<Path> reportFilesAfterCleanUp = cleaner.findAllReportsOf(jobName);
        assertEquals(1, reportFilesAfterCleanUp.size());
    }

    public void test_cleanup_without_diff() throws IOException, MaterialstoreException {
        throw new RuntimeException("TODO");
    }

    /**
     * make sure cleanup(JobName jobName, JobTimestamp olderThan) will delete
     * older artifacts while retaining the unit(s) of artifacts
     * newer or equal to the "olderThan".
     */
    @Test
    public void test_cleanup_with_boundaryJobTimestamp()
            throws IOException, MaterialstoreException {
        String testCaseName = "test_cleanup_with_boundaryJobTimestamp";
        Store store = FixtureDirCopier.copyIssue334FixtureInto(testClassOutputDir.resolve(testCaseName));
        StoreCleaner cleaner = StoreCleaner.newInstance(store);
        JobName jobName = new JobName("CURA");
        // Assert before
        List<JobTimestamp> jobTimestampsBeforeCleanUp = store.findAllJobTimestamps(jobName);
        assertEquals(7, jobTimestampsBeforeCleanUp.size());
        List<Path> reportFilesBeforeCleanUp = cleaner.findAllReportsOf(jobName);
        assertEquals(3, reportFilesBeforeCleanUp.size());
        // Action
        cleaner.cleanup(jobName, new JobTimestamp("20221026_171412"));
        // Assert after
        List<JobTimestamp> jobTimestampsAfterCleanUp = store.findAllJobTimestamps(jobName);
        assertEquals(5, jobTimestampsAfterCleanUp.size());
        List<Path> reportFilesAfterCleanUp = cleaner.findAllReportsOf(jobName);
        assertEquals(2, reportFilesAfterCleanUp.size());
    }

    /**
     * make sure cleanup(JobName jobName, int olderThan) will delete
     * older artifacts while retaining the unit(s) of artifacts
     * newer than or equal to the nth.
     */
    @Test
    public void test_cleanup_with_numberOfJobTimestamps()
            throws IOException, MaterialstoreException {
        String testCaseName = "test_cleanup_with_numberOfJobTimestamps";
        Store store = FixtureDirCopier.copyIssue334FixtureInto(testClassOutputDir.resolve(testCaseName));
        StoreCleaner cleaner = StoreCleaner.newInstance(store);
        JobName jobName = new JobName("CURA");
        // Assert before
        List<JobTimestamp> jobTimestampsBeforeCleanUp = store.findAllJobTimestamps(jobName);
        assertEquals(7, jobTimestampsBeforeCleanUp.size());
        List<Path> reportFilesBeforeCleanUp = cleaner.findAllReportsOf(jobName);
        assertEquals(3, reportFilesBeforeCleanUp.size());
        // Action
        cleaner.cleanup(jobName, 2);
        // Assert after
        List<JobTimestamp> jobTimestampsAfterCleanUp = store.findAllJobTimestamps(jobName);
        assertEquals(5, jobTimestampsAfterCleanUp.size());
        List<Path> reportFilesAfterCleanUp = cleaner.findAllReportsOf(jobName);
        assertEquals(2, reportFilesAfterCleanUp.size());
    }


}