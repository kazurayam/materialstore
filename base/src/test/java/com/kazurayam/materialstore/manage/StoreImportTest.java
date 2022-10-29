package com.kazurayam.materialstore.manage;

import com.kazurayam.materialstore.FixtureDirCopier;
import com.kazurayam.materialstore.TestHelper;
import java.nio.file.Path;

import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.filesystem.Stores;
import org.apache.commons.lang3.builder.ToStringExclude;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StoreImportTest {

    private Path testClassOutputDir;

    @BeforeEach
    public void beforeEach() throws IOException {
        testClassOutputDir = TestHelper.createTestClassOutputDir(this);
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
