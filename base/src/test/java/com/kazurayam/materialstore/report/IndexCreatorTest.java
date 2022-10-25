package com.kazurayam.materialstore.report;

import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.Store;
import java.nio.file.Path;

import com.kazurayam.materialstore.inspector.Inspector;
import com.kazurayam.materialstore.manage.StoreCleaner;
import com.kazurayam.materialstore.reduce.MaterialProductGroup;
import com.kazurayam.materialstore.reduce.Reducer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.kazurayam.materialstore.TestCaseSupport;

import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class IndexCreatorTest {

    private TestCaseSupport tcSupport;
    private Store store;
    private JobName jobName;

    private IndexCreator indexCreator;

    @BeforeEach
    public void beforeEach() throws MaterialstoreException {
        tcSupport = new TestCaseSupport(this);
        store = tcSupport.getStore();
        indexCreator = new IndexCreator(store);
    }

    @Test
    public void test_create() throws MaterialstoreException, IOException {
        jobName = new JobName("test_create");
        JobTimestamp jtA = tcSupport.create3TXTsWithStepAndLabel(jobName, JobTimestamp.now());
        JobTimestamp jtB = tcSupport.create3TXTsWithStepAndLabel(jobName, JobTimestamp.laterThan(jtA)); // intentionally create 2 JobTimestamps
        MaterialList mlA = store.select(jobName, jtA);
        MaterialList mlB = store.select(jobName, jtB);
        MaterialProductGroup reduced = Reducer.chronos(store, mlB);
        Inspector inspector = Inspector.newInstance(store);
        MaterialProductGroup inspected = inspector.reduceAndSort(reduced);
        Path report = inspector.report(inspected, 0.0);
        //
        JobTimestamp oldestJT = store.findNthJobTimestamp(jobName, 4);
        StoreCleaner scavenger = StoreCleaner.newInstance(store);
        scavenger.deleteJobTimestampsOlderThan(jobName, oldestJT);
        //
        Path indexFile = indexCreator.create();
        assertTrue(Files.exists(indexFile));
    }
}
