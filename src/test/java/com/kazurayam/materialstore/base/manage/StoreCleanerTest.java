package com.kazurayam.materialstore.base.manage;

import com.kazurayam.materialstore.TestOutputOrganizerFactory;
import com.kazurayam.materialstore.base.FixtureDirCopier;
import com.kazurayam.materialstore.TestFixtureSupport;
import com.kazurayam.materialstore.TestHelper;
import com.kazurayam.materialstore.core.JobName;
import com.kazurayam.materialstore.core.JobNameNotFoundException;
import com.kazurayam.materialstore.core.JobTimestamp;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.Store;
import com.kazurayam.materialstore.core.Stores;
import com.kazurayam.unittest.TestOutputOrganizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StoreCleanerTest {

    private static final Logger log = LoggerFactory.getLogger(StoreCleanerTest.class);
    private static final TestOutputOrganizer too = TestOutputOrganizerFactory.create(StoreCleanerTest.class);
    private Path classOutputDir;

    @BeforeEach
    public void beforeEach() throws IOException {
        too.cleanClassOutputDirectory();
        classOutputDir = too.getClassOutputDirectory();
    }

    /**
     * make sure cleanup(JobName) without 2nd argument
     * will retain the latest unit of artifacts
     */
    @Test
    public void test_cleanup_with_diff() throws IOException, MaterialstoreException, JobNameNotFoundException {
        String testCaseName = "test_cleanup_with_diff";
        Store store = FixtureDirCopier.copyIssue334FixtureInto(
                classOutputDir.resolve(testCaseName));
        StoreCleaner cleaner = StoreCleaner.newInstance(store);
        // Action
        JobName jobName = new JobName("CURA");
        cleaner.cleanup(jobName);
        // Assert
        List<JobTimestamp> jobTimestampsAfterCleanUp = store.findAllJobTimestamps(jobName);
        assertEquals(3, jobTimestampsAfterCleanUp.size());
        List<Path> reportFilesAfterCleanUp = store.findAllReportsOf(jobName);
        assertEquals(1, reportFilesAfterCleanUp.size());
    }

    @Test
    public void test_cleanup_MaterialListReports_olderThan() throws MaterialstoreException, JobNameNotFoundException {
        String testCaseName = "test_cleanup_MaterialListReports_olderThan";
        // Arrange
        Path testCaseOutputDir = classOutputDir.resolve(testCaseName);
        Store store = Stores.newInstance(testCaseOutputDir.resolve("store"));
        JobName jobName = new JobName(testCaseName);
        JobTimestamp jtA = new JobTimestamp("20221026_205509");
        JobTimestamp jtB = new JobTimestamp("20221029_220401");
        TestFixtureSupport.create3TXTs(store, jobName, jtA);
        TestFixtureSupport.create3TXTs(store, jobName, jtB);
        // Action
        StoreCleaner cleaner = StoreCleaner.newInstance(store);
        JobTimestamp olderThan = jtB.minusHours(2);

        log.debug("jtA      =" + jtA);
        log.debug("jtB      =" + jtB);
        log.debug("olderThan=" + olderThan);

        cleaner.cleanup(jobName, olderThan);
        // Assert
        //assertEquals(1, store.findAllReportsOf(jobName).size());
        assertEquals(1, store.findAllJobTimestamps(jobName).size());
    }

    /**
     * make sure cleanup(JobName jobName, JobTimestamp olderThan) will delete
     * older artifacts while retaining the unit(s) of artifacts
     * newer or equal to the "olderThan".
     */
    @Test
    public void test_cleanup_with_boundaryJobTimestamp()
            throws IOException, MaterialstoreException, JobNameNotFoundException {
        String testCaseName = "test_cleanup_with_boundaryJobTimestamp";
        Store store = FixtureDirCopier.copyIssue334FixtureInto(classOutputDir.resolve(testCaseName));
        StoreCleaner cleaner = StoreCleaner.newInstance(store);
        JobName jobName = new JobName("CURA");
        // Assert before
        List<JobTimestamp> jobTimestampsBeforeCleanUp = store.findAllJobTimestamps(jobName);
        assertEquals(7, jobTimestampsBeforeCleanUp.size());
        List<Path> reportFilesBeforeCleanUp = store.findAllReportsOf(jobName);
        assertEquals(3, reportFilesBeforeCleanUp.size());
        // Action
        cleaner.cleanup(jobName, new JobTimestamp("20221026_171412"));
        // Assert after
        List<JobTimestamp> jobTimestampsAfterCleanUp = store.findAllJobTimestamps(jobName);
        assertEquals(5, jobTimestampsAfterCleanUp.size());
        List<Path> reportFilesAfterCleanUp = store.findAllReportsOf(jobName);
        assertEquals(2, reportFilesAfterCleanUp.size());
    }

    /**
     * make sure cleanup(JobName jobName, int olderThan) will delete
     * older artifacts while retaining the unit(s) of artifacts
     * newer than or equal to the nth.
     */
    @Test
    public void test_cleanup_with_numberOfJobTimestamps()
            throws IOException, MaterialstoreException, JobNameNotFoundException {
        String testCaseName = "test_cleanup_with_numberOfJobTimestamps";
        Store store = FixtureDirCopier.copyIssue334FixtureInto(classOutputDir.resolve(testCaseName));
        StoreCleaner cleaner = StoreCleaner.newInstance(store);
        JobName jobName = new JobName("CURA");
        // Assert before
        List<JobTimestamp> jobTimestampsBeforeCleanUp = store.findAllJobTimestamps(jobName);
        assertEquals(7, jobTimestampsBeforeCleanUp.size());
        List<Path> reportFilesBeforeCleanUp = store.findAllReportsOf(jobName);
        assertEquals(3, reportFilesBeforeCleanUp.size());
        // Action
        cleaner.cleanup(jobName, 2);
        // Assert after
        List<JobTimestamp> jobTimestampsAfterCleanUp = store.findAllJobTimestamps(jobName);
        assertEquals(5, jobTimestampsAfterCleanUp.size());
        List<Path> reportFilesAfterCleanUp = store.findAllReportsOf(jobName);
        assertEquals(2, reportFilesAfterCleanUp.size());
    }

    @Test
    public void test_deleteJobTimestampsOlderThan()
            throws MaterialstoreException, JobNameNotFoundException {
        JobName jobName = new JobName("test_deleteJobTimestampsOlderThan");
        Path testCaseDir = classOutputDir.resolve(jobName.toString());
        Store store = Stores.newInstance(testCaseDir.resolve("store"));
        JobTimestamp jtA = new JobTimestamp("20221026_205509");
        JobTimestamp jtB = new JobTimestamp("20221029_220401");
        TestFixtureSupport.create3TXTs(store, jobName, jtA);
        TestFixtureSupport.create3TXTs(store, jobName, jtB);

        List<JobTimestamp> jobTimestampList = store.findAllJobTimestamps(jobName);
        assertTrue(jobTimestampList.size() >= 2,
                "jobTimestampList.size()=" + jobTimestampList.size());
        // delete JobTimestamp directories older than the one last 1 JobTimestamp.
        StoreCleaner cleaner = StoreCleaner.newInstance(store);
        int deleted = cleaner.deleteJobTimestampsOlderThan(jobName, store.findNthJobTimestamp(jobName, 1));
        assertTrue(deleted >= 1);
        assertEquals(1, store.findAllJobTimestamps(jobName).size());
    }

    @Test
    public void test_deleteReportsOlderThan() throws IOException, MaterialstoreException {
        JobName jobName = new JobName("test_deleteReportsOlderThan");
        Path testCaseDir = classOutputDir.resolve(jobName.toString());
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
        assertEquals(1, store.findAllReportsOf(jobName).size());
    }
}