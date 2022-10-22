package com.kazurayam.materialstore.report;

import com.kazurayam.materialstore.FixtureCreator;
import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.Store;
import java.nio.file.Path;
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
        Path indexFile = indexCreator.create();
        assertTrue(Files.exists(indexFile));
    }
}
