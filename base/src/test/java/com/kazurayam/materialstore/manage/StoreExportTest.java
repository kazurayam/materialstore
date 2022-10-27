package com.kazurayam.materialstore.manage;

import com.kazurayam.materialstore.FixtureDirCopier;
import com.kazurayam.materialstore.TestHelper;
import java.nio.file.Path;

import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.filesystem.Stores;
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
                String.format("JobName %s is not found in %s", jobName, remote));
        assertEquals(3, remote.findAllJobTimestamps(jobName).size());
        assertEquals(1, remote.findAllReportsOf(jobName).size());
    }

    @Test
    public void test_exportReports_newerThanOrEqualTo() {

    }

}
