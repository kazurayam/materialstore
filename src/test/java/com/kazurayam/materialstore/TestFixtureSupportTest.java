package com.kazurayam.materialstore;

import com.kazurayam.materialstore.core.JobName;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.Store;
import com.kazurayam.materialstore.core.Stores;
import com.kazurayam.unittest.TestOutputOrganizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestFixtureSupportTest {

    private static final TestOutputOrganizer too =
            TestOutputOrganizerFactory.create(TestFixtureSupportTest.class);
    private Path testClassOutputDir;
    private Store store;

    @BeforeEach
    public void setup() throws IOException {
        Path root = too.getClassOutputDirectory().resolve("store");
        store = Stores.newInstance(root);
    }

    @Test
    public void test_copyFixture() throws MaterialstoreException, IOException {
        Path fixtureDir = TestHelper.getFixturesDirectory().resolve("issue#331");
        too.copyDir(fixtureDir, store.getRoot());
        assertTrue(store.contains(new JobName("CURA")));
    }
}
