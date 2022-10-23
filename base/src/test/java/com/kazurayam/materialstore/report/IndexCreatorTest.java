package com.kazurayam.materialstore.report;

import com.kazurayam.materialstore.FixtureCreator;
import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.Store;
import java.nio.file.Path;

import com.kazurayam.materialstore.inspector.Inspector;
import com.kazurayam.materialstore.reduce.MaterialProductGroup;
import com.kazurayam.materialstore.reduce.Reducer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.kazurayam.materialstore.TestHelper;

import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class IndexCreatorTest {

    private Store store;
    private JobName jobName;

    private IndexCreator indexCreator;

    @BeforeAll
    public static void beforeAll() throws IOException {
    }

    @BeforeEach
    public void beforeEach() throws IOException, MaterialstoreException {
        store = TestHelper.initializeStore(this);
        indexCreator = new IndexCreator(store);
    }

    @Test
    public void test_create() throws MaterialstoreException, IOException {
        jobName = new JobName("test_create");
        JobTimestamp jtA = FixtureCreator.createFixtures(store, jobName, JobTimestamp.now());
        JobTimestamp jtB = FixtureCreator.createFixtures(store, jobName, jtA); // intentionally create 2 JobTimestamps
        MaterialList mlA = store.select(jobName, jtA);
        MaterialList mlB = store.select(jobName, jtB);
        MaterialProductGroup reduced = Reducer.chronos(store, mlB);
        Inspector inspector = Inspector.newInstance(store);
        MaterialProductGroup inspected = inspector.reduceAndSort(reduced);
        Path report = inspector.report(inspected, 0.0);
        //
        JobTimestamp oldestJT = store.findNthJobTimestamp(jobName, 4);
        store.deleteStuffOlderThanExclusive(jobName, oldestJT);
        //
        Path indexFile = indexCreator.create();
        assertTrue(Files.exists(indexFile));
    }
}
