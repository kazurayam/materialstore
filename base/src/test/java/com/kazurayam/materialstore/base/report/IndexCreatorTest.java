package com.kazurayam.materialstore.base.report;

import com.kazurayam.materialstore.core.TestHelper;
import com.kazurayam.materialstore.core.filesystem.JobName;
import com.kazurayam.materialstore.core.filesystem.JobTimestamp;
import com.kazurayam.materialstore.core.filesystem.MaterialList;
import com.kazurayam.materialstore.core.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.core.filesystem.Store;
import java.nio.file.Path;

import com.kazurayam.materialstore.core.filesystem.Stores;
import com.kazurayam.materialstore.base.inspector.Inspector;
import com.kazurayam.materialstore.base.manage.StoreCleaner;
import com.kazurayam.materialstore.base.reduce.MaterialProductGroup;
import com.kazurayam.materialstore.base.reduce.Reducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.kazurayam.materialstore.core.TestFixtureSupport;

import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class IndexCreatorTest {

    private Path testClassOutputDir;
    private Store store;

    private IndexCreator indexCreator;

    @BeforeEach
    public void beforeEach() throws MaterialstoreException {
        testClassOutputDir = TestHelper.createTestClassOutputDir(this);
        store = Stores.newInstance(testClassOutputDir.resolve("store"));
        indexCreator = new IndexCreator(store);
    }

    @Test
    public void test_create() throws MaterialstoreException, IOException {
        JobName jobName = new JobName("test_create");
        JobTimestamp jtA = TestFixtureSupport.create3TXTs(store, jobName, JobTimestamp.now());
        JobTimestamp jtB = TestFixtureSupport.create3TXTs(store, jobName, JobTimestamp.laterThan(jtA)); // intentionally create 2 JobTimestamps
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
