package com.kazurayam.materialstore.base.manage;

import com.kazurayam.materialstore.TestOutputOrganizerFactory;
import com.kazurayam.materialstore.base.FixtureDirCopier;
import com.kazurayam.materialstore.TestHelper;
import com.kazurayam.materialstore.core.JobName;
import com.kazurayam.materialstore.core.JobNameNotFoundException;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.Store;
import com.kazurayam.materialstore.core.Stores;
import com.kazurayam.unittest.TestOutputOrganizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StoreExportTest {

    private static final TestOutputOrganizer too = TestOutputOrganizerFactory.create(StoreExportTest.class);

    @BeforeEach
    public void beforeEach() throws IOException {
        too.cleanClassOutputDirectory();
    }

    @Test
    public void test_exportReports_latest() throws IOException, MaterialstoreException, JobNameNotFoundException {
        // Arrange
        Path methodOutputDir = too.getMethodOutputDirectory("test_exportReports_latest");
        Store local = FixtureDirCopier.copyIssue334FixtureInto(methodOutputDir);
        Store remote = Stores.newInstance(methodOutputDir.resolve("remote"));
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
