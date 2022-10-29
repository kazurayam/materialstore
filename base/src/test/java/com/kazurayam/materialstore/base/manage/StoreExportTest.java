package com.kazurayam.materialstore.base.manage;

import com.kazurayam.materialstore.base.FixtureDirCopier;
import com.kazurayam.materialstore.core.TestHelper;
import java.nio.file.Path;

import com.kazurayam.materialstore.core.filesystem.JobName;
import com.kazurayam.materialstore.core.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.core.filesystem.Store;
import com.kazurayam.materialstore.core.filesystem.Stores;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StoreExportTest {

    private Path testClassOutputDir;

    @BeforeEach
    public void beforeEach() throws IOException {
        testClassOutputDir = TestHelper.createTestClassOutputDir(this);
    }

    @Test
    public void test_exportReports_latest() throws IOException, MaterialstoreException {
        // Arrange
        String testCaseName = "test_exportReports_latest";
        Path testCaseOutputDir = testClassOutputDir.resolve(testCaseName);
        Store local = FixtureDirCopier.copyIssue334FixtureInto(testCaseOutputDir);
        Store remote = Stores.newInstance(testCaseOutputDir.resolve("remote"));
        // Action
        StoreExport storeExport = StoreExport.newInstance(local, remote);
        JobName jobName = new JobName("CURA");
        storeExport.exportReports(jobName);
        // Assert
        assertTrue(remote.findAllJobNames().contains(jobName),
                String.format("JobName %s is not found in the remote store at %s", jobName, remote));
        assertEquals(3, remote.findAllJobTimestamps(jobName).size());
        assertEquals(1, remote.findAllReportsOf(jobName).size());
    }

}
