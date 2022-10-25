package com.kazurayam.materialstore.filesystem;

import com.kazurayam.materialstore.TestCaseSupport;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StoreTest {

    private static TestCaseSupport tcSupport;
    private Store store;
    private JobName jobName;

    @BeforeEach
    public void beforeEach() throws IOException {
        tcSupport = new TestCaseSupport(this);
        store = tcSupport.getStore();
    }

    @Test
    public void test_contains_JobName() throws MaterialstoreException, IOException {
        jobName = new JobName("test_contains_JobName");
        JobTimestamp jtA = tcSupport.create3TXTsWithStepAndLabel(jobName, JobTimestamp.now());
        assertTrue(store.contains(jobName));
    }

    @Test
    public void test_findAllJobNames() throws MaterialstoreException, IOException {
        jobName = new JobName("test_contains_JobName");
        JobTimestamp jtA = tcSupport.create3TXTsWithStepAndLabel(jobName, JobTimestamp.now());
        assertTrue(store.findAllJobNames().size() > 0);
        assertTrue(store.findAllJobNames().contains(jobName));
    }

    @Test
    public void test_deleteJobName() throws MaterialstoreException, IOException {
        jobName = new JobName("test_deleteJobName");
        JobTimestamp jtA = tcSupport.create3TXTsWithStepAndLabel(jobName, JobTimestamp.now());
        assertTrue(store.contains(jobName));
        //
        int i = store.deleteJobName(jobName);
        assertFalse(store.contains(jobName));
    }

    @Test
    public void test_findNthJobTimestamp_normal() throws MaterialstoreException, IOException {
        jobName = new JobName("test_findNthJobTimestamp_normal");
        JobTimestamp jtA = tcSupport.create3TXTsWithStepAndLabel(jobName, JobTimestamp.now());
        JobTimestamp jtB = tcSupport.create3TXTsWithStepAndLabel(jobName, JobTimestamp.laterThan(jtA)); // intentionally create 2 JobTimestamps
        List<JobTimestamp> jobTimestampList = store.findAllJobTimestamps(jobName);
        assertTrue(jobTimestampList.size() >= 2);
        // findNthJobTimestamps regards the list of JobTimestamp in the descending order
        assertEquals(jtB, store.findNthJobTimestamp(jobName, 1));
        assertEquals(jtA, store.findNthJobTimestamp(jobName, 2));
    }

    @Test
    public void test_findNthJobTimestamp_exceedingRange() throws MaterialstoreException, IOException {
        jobName = new JobName("test_findNthJobTimestamp_exceedingRange");
        JobTimestamp jtA = tcSupport.create3PNGsWithStepAndLabel(jobName, JobTimestamp.now());
        JobTimestamp jtB = tcSupport.create3PNGsWithStepAndLabel(jobName, JobTimestamp.laterThan(jtA)); // intentionally create 2 JobTimestamps
        List<JobTimestamp> jobTimestampList = store.findAllJobTimestamps(jobName);
        assertTrue(jobTimestampList.size() >= 2);
        // if nth parameter exceeds the range, return the last jobTimestamp
        assertEquals(jtB, store.findNthJobTimestamp(jobName, 999));
    }

}
