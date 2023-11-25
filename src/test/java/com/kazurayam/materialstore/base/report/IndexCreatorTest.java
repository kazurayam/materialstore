package com.kazurayam.materialstore.base.report;

import com.kazurayam.materialstore.base.inspector.Inspector;
import com.kazurayam.materialstore.base.manage.StoreCleaner;
import com.kazurayam.materialstore.base.reduce.MaterialProductGroup;
import com.kazurayam.materialstore.base.reduce.Reducer;
import com.kazurayam.materialstore.TestFixtureSupport;
import com.kazurayam.materialstore.TestHelper;
import com.kazurayam.materialstore.core.JobName;
import com.kazurayam.materialstore.core.JobNameNotFoundException;
import com.kazurayam.materialstore.core.JobTimestamp;
import com.kazurayam.materialstore.core.MaterialList;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.Store;
import com.kazurayam.materialstore.core.Stores;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IndexCreatorTest {

    private Path testClassOutputDir;
    private Store store;

    private IndexCreator indexCreator;

    @BeforeEach
    public void beforeEach() throws MaterialstoreException {
        testClassOutputDir = TestHelper.createTestClassOutputDir(IndexCreatorTest.class);
        store = Stores.newInstance(testClassOutputDir.resolve("store"));
        indexCreator = new IndexCreator(store);
    }

    @Test
    public void test_create() throws MaterialstoreException, IOException, JobNameNotFoundException {
        JobName jobName = new JobName("test_create");
        JobTimestamp jtA = TestFixtureSupport.create3TXTs(store, jobName, JobTimestamp.now());
        JobTimestamp jtB = TestFixtureSupport.create3TXTs(store, jobName, JobTimestamp.laterThan(jtA)); // intentionally create 2 JobTimestamps
        //MaterialList mlA = store.select(jobName, jtA);
        MaterialList mlB = store.select(jobName, jtB);
        MaterialProductGroup reduced = Reducer.chronos(store, mlB);
        Inspector inspector = Inspector.newInstance(store);
        MaterialProductGroup inspected = inspector.reduceAndSort(reduced);
        //Path report = inspector.report(inspected, 0.0);
        //
        JobTimestamp oldestJT = store.findNthJobTimestamp(jobName, 4);
        StoreCleaner scavenger = StoreCleaner.newInstance(store);
        scavenger.deleteJobTimestampsOlderThan(jobName, oldestJT);
        //
        Path indexFile = indexCreator.create();
        assertTrue(Files.exists(indexFile));
    }

    @Test
    public void test_makeTitle() throws IOException {
        Map<String, Object> model = new HashMap<>();
        model.put("store", "/Users/foo/tmp/myProject/store-backup");
        String title = indexCreator.makeTitle(model);
        System.out.println(title);
        assertEquals("store-backup/index.html", title);
    }
}
