package com.kazurayam.materialstore.filesystem;

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
        TestHelper.initializeOutputDir();
    }

    @BeforeEach
    public void beforeEach() throws IOException, MaterialstoreException {
        store = TestHelper.initializeStore(this);
    }

    @Test
    public void test_findNthJobTimestamp_normal() throws MaterialstoreException, IOException {
        jobName = new JobName("test_findNthJobTimestamp_normal");
        JobTimestamp jtA = createFixtures(store, jobName, JobTimestamp.now());
        JobTimestamp jtB = createFixtures(store, jobName, jtA); // intentionally create 2 JobTimestamps
        List<JobTimestamp> jobTimestampList = store.findAllJobTimestamps(jobName);
        assertTrue(jobTimestampList.size() >= 2);
        // findNthJobTimestamps regards the list of JobTimestamp in the descending order
        assertEquals(jtB, store.findNthJobTimestamp(jobName, 1));
        assertEquals(jtA, store.findNthJobTimestamp(jobName, 2));
    }

    @Test
    public void test_findNthJobTimestamp_exceedingRange() throws MaterialstoreException, IOException {
        jobName = new JobName("test_findNthJobTimestamp_exceedingRange");
        JobTimestamp jtA = createFixtures(store, jobName, JobTimestamp.now());
        JobTimestamp jtB = createFixtures(store, jobName, jtA); // intentionally create 2 JobTimestamps
        List<JobTimestamp> jobTimestampList = store.findAllJobTimestamps(jobName);
        assertTrue(jobTimestampList.size() >= 2);
        // if nth parameter exceeds the range, return the last jobTimestamp
        assertEquals(jtB, store.findNthJobTimestamp(jobName, 999));
    }

    @Test
    public void test_deleteStuffOlderThanExclusive() throws MaterialstoreException, IOException {
        jobName = new JobName("test_deleteStuffOlderThanExclusive");
        JobTimestamp jtA = createFixtures(store, jobName, JobTimestamp.now());
        JobTimestamp jtB = createFixtures(store, jobName, jtA); // intentionally create 2 JobTimestamps
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

    /**
     *
     * @param store
     * @param jobName
     */
    private JobTimestamp createFixtures(Store store, JobName jobName, JobTimestamp base) throws MaterialstoreException, IOException {
        JobTimestamp jobTimestamp = JobTimestamp.laterThan(base);
        Material apple = this.writeFixtureIntoStore(store, jobName, jobTimestamp, "Apple", "01", "it is red");
        Material orange = this.writeFixtureIntoStore(store, jobName, jobTimestamp, "Orange", "02", "it is orange");
        Material money = this.writeFixtureIntoStore(store, jobName, jobTimestamp, "Money", "03", "it is green");
        return jobTimestamp;
    }

    /**
     *
     */
    private Material writeFixtureIntoStore(Store store,
                                        JobName jobName,
                                        JobTimestamp jobTimestamp,
                                        String text,
                                        String step,
                                        String label) throws MaterialstoreException {
        Metadata metadata =
                new Metadata.Builder()
                        .put("step", step)
                        .put("label", label).build();
        return store.write(jobName, jobTimestamp, FileType.TXT, metadata, text);
    }
}
