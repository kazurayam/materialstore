package com.kazurayam.materialstore.base.manage;

import com.kazurayam.materialstore.base.FixtureDirCopier;
import com.kazurayam.materialstore.core.TestHelper;
import com.kazurayam.materialstore.core.filesystem.JobName;
import com.kazurayam.materialstore.core.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.core.filesystem.Store;
import com.kazurayam.materialstore.core.filesystem.Stores;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StoreImportTest {

    private Path testClassOutputDir;

    @BeforeEach
    public void beforeEach() throws IOException {
        testClassOutputDir = TestHelper.createTestClassOutputDir(StoreImportTest.class);
    }

    @Test
    public void test_importReports_latest() throws IOException, MaterialstoreException {
        // Arrange
        String testCaseName = "test_importReports_latest";
        Path testCaseOutputDir = testClassOutputDir.resolve(testCaseName);
        Store remote = FixtureDirCopier.copyIssue334FixtureInto(testCaseOutputDir);
        Store local = Stores.newInstance(testCaseOutputDir.resolve("local"));
        // Action
        StoreImport storeImport = StoreImport.newInstance(remote, local);
        JobName jobName = new JobName("CURA");
        storeImport.importReports(jobName);
        // Assert
        assertTrue(local.findAllJobNames().contains(jobName),
                String.format("JobName %s is not found in the local store at %s", jobName, local));
        assertEquals(3, local.findAllJobTimestamps(jobName).size());
        assertEquals(1, local.findAllReportsOf(jobName).size());
    }

}
