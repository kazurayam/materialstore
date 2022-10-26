package com.kazurayam.materialstore;

import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestCaseSupportTest {

    TestCaseSupport tcSupport;
    Store store;

    @BeforeEach
    public void setup() {
        tcSupport = new TestCaseSupport(this);
        store = tcSupport.getStore();
    }

    @Test
    public void test_copyFixture() throws MaterialstoreException, IOException {
        Path fixtureDir = TestHelper.getFixturesDirectory().resolve("issue#331");
        TestHelper.copyDirectory(fixtureDir, tcSupport.getOutputDir());
        assertTrue(store.contains(new JobName("CURA")));
    }
}
