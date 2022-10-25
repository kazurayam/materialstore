package com.kazurayam.materialstore.filesystem;

import com.kazurayam.materialstore.TestCaseSupport;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StoreTest {

    private TestCaseSupport tcSupport;
    private Store store;
    private JobName jobName;

    @BeforeEach
    public void beforeEach() throws IOException {
        tcSupport = new TestCaseSupport(this);
        store = tcSupport.getStore();
    }

    @Test
    public void test_contains_JobName() throws MaterialstoreException {
        jobName = new JobName("test_contains_JobName");
        JobTimestamp jtA = tcSupport.create3TXTsWithStepAndLabel(jobName, JobTimestamp.now());
        assertTrue(store.contains(jobName));
    }

    @Test
    public void test_findAllJobNames() throws MaterialstoreException {
        jobName = new JobName("test_contains_JobName");
        JobTimestamp jtA = tcSupport.create3TXTsWithStepAndLabel(jobName, JobTimestamp.now());
        assertTrue(store.findAllJobNames().size() > 0);
        assertTrue(store.findAllJobNames().contains(jobName));
    }

    @Test
    public void test_deleteJobName() throws MaterialstoreException {
        jobName = new JobName("test_deleteJobName");
        JobTimestamp jtA = tcSupport.create3TXTsWithStepAndLabel(jobName, JobTimestamp.now());
        assertTrue(store.contains(jobName));
        //
        int i = store.deleteJobName(jobName);
        assertFalse(store.contains(jobName));
    }

    @Test
    public void test_findNthJobTimestamp_normal() throws MaterialstoreException {
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
    public void test_findNthJobTimestamp_exceedingRange() throws MaterialstoreException {
        jobName = new JobName("test_findNthJobTimestamp_exceedingRange");
        JobTimestamp jtA = tcSupport.create3PNGsWithStepAndLabel(jobName, JobTimestamp.now());
        JobTimestamp jtB = tcSupport.create3PNGsWithStepAndLabel(jobName, JobTimestamp.laterThan(jtA)); // intentionally create 2 JobTimestamps
        List<JobTimestamp> jobTimestampList = store.findAllJobTimestamps(jobName);
        assertTrue(jobTimestampList.size() >= 2);
        // if nth parameter exceeds the range, return the last jobTimestamp
        assertEquals(jtB, store.findNthJobTimestamp(jobName, 999));
    }

    @Test
    public void test_getPathOf_JobName() throws MaterialstoreException {
        jobName = new JobName("test_getPathOf_JobName");
        JobTimestamp jtA = tcSupport.create3TXTsWithStepAndLabel(jobName, JobTimestamp.now());
        //
        Path jobNamePath = store.getPathOf(jobName);
        assertNotNull(jobNamePath);
        assertTrue(jobNamePath.toString().endsWith(jobName.toString()));
    }

    @Test
    public void test_getPathOf_JobTimestamp() throws MaterialstoreException {
        jobName = new JobName("test_getPathOf_JobTimestamp");
        JobTimestamp jobTimestamp = tcSupport.create3TXTsWithStepAndLabel(jobName, JobTimestamp.now());
        //
        Path jobTimestampPath = store.getPathOf(jobName, jobTimestamp);
        assertNotNull(jobTimestampPath);
        assertTrue(jobTimestampPath.toString().endsWith(jobTimestamp.toString()));
    }
}