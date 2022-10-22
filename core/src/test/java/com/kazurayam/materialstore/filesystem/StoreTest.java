package com.kazurayam.materialstore.filesystem;

import com.kazurayam.materialstore.FixtureCreator;
import com.kazurayam.materialstore.TestHelper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StoreTest {

    private Store store;
    private JobName jobName;
    private JobTimestamp jobTimestamp;

    @BeforeAll
    public static void beforeAll() throws IOException {
    }

    @BeforeEach
    public void beforeEach() throws IOException, MaterialstoreException {
        store = TestHelper.initializeStore(this);
    }

    @Test
    public void test_findNthJobTimestamp_normal() throws MaterialstoreException, IOException {
        jobName = new JobName("test_findNthJobTimestamp_normal");
        JobTimestamp jtA = FixtureCreator.createFixtures(store, jobName, JobTimestamp.now());
        JobTimestamp jtB = FixtureCreator.createFixtures(store, jobName, jtA); // intentionally create 2 JobTimestamps
        List<JobTimestamp> jobTimestampList = store.findAllJobTimestamps(jobName);
        assertTrue(jobTimestampList.size() >= 2);
        // findNthJobTimestamps regards the list of JobTimestamp in the descending order
        assertEquals(jtB, store.findNthJobTimestamp(jobName, 1));
        assertEquals(jtA, store.findNthJobTimestamp(jobName, 2));
    }

    @Test
    public void test_findNthJobTimestamp_exceedingRange() throws MaterialstoreException, IOException {
        jobName = new JobName("test_findNthJobTimestamp_exceedingRange");
        JobTimestamp jtA = FixtureCreator.createFixtures(store, jobName, JobTimestamp.now());
        JobTimestamp jtB = FixtureCreator.createFixtures(store, jobName, jtA); // intentionally create 2 JobTimestamps
        List<JobTimestamp> jobTimestampList = store.findAllJobTimestamps(jobName);
        assertTrue(jobTimestampList.size() >= 2);
        // if nth parameter exceeds the range, return the last jobTimestamp
        assertEquals(jtB, store.findNthJobTimestamp(jobName, 999));
    }

    @Test
    public void test_deleteStuffOlderThanExclusive() throws MaterialstoreException, IOException {
        jobName = new JobName("test_deleteStuffOlderThanExclusive");
        JobTimestamp jtA = FixtureCreator.createFixtures(store, jobName, JobTimestamp.now());
        JobTimestamp jtB = FixtureCreator.createFixtures(store, jobName, jtA); // intentionally create 2 JobTimestamps
        List<JobTimestamp> jobTimestampList = store.findAllJobTimestamps(jobName);
        assertTrue(jobTimestampList.size() >= 2);
        // create the report HTML file
        JobTimestamp htmlJobTimestamp = JobTimestamp.laterThan(jtB);
        Path report = store.getRoot().resolve(store.resolveReportFileName(jobName, htmlJobTimestamp));
        Files.write(report, "<html></html>".getBytes());
        // delete JobTimestamp directories older than the one last 1 JobTimestamp.
        // delete <JobName>-<JobTimestamp>.html files older than the one last 1 JobTimestamp
        int deleted = store.deleteStuffOlderThanExclusive(jobName, store.findNthJobTimestamp(jobName, 1));
        assertTrue(deleted >= 1);
    }

}
