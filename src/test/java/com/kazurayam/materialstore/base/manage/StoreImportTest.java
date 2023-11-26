package com.kazurayam.materialstore.base.manage;

import com.kazurayam.materialstore.TestOutputOrganizerFactory;
import com.kazurayam.materialstore.base.FixtureDirCopier;
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

public class StoreImportTest {

    private static final TestOutputOrganizer too = TestOutputOrganizerFactory.create(StoreImportTest.class);

    @BeforeEach
    public void beforeEach() throws IOException {
        too.cleanClassOutputDirectory();
    }

    @Test
    public void test_importReports_latest() throws IOException, MaterialstoreException, JobNameNotFoundException {
        // Arrange
        Path methodOutputDir = too.getMethodOutputDirectory("test_importReports_latest");
        Store remote = FixtureDirCopier.copyIssue334FixtureInto(methodOutputDir);
        Store local = Stores.newInstance(methodOutputDir.resolve("local"));
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
