package com.kazurayam.materialstore.util;

import com.kazurayam.materialstore.TestOutputOrganizerFactory;
import com.kazurayam.materialstore.core.JobName;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.Store;
import com.kazurayam.unittest.TestOutputOrganizer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestFixtureUtil {

    private static final TestOutputOrganizer too =
            TestOutputOrganizerFactory.create(TestFixtureUtil.class);
    private static Path resultsDir =
            too.getProjectDir().resolve("src/test/fixtures/sample_results");

    public static void setupFixture(Store store, JobName jobName) throws MaterialstoreException {
        try {
            Path jobNameDir = store.getRoot().resolve(jobName.toString());
            // make sure the Job directory to be empty
            if (Files.exists(jobNameDir)) {
                too.deleteDir(store.getRoot().resolve(jobName.toString()));
            }
            // stuff the Job directory with a fixture
            too.copyDir(resultsDir, jobNameDir);
        } catch (IOException e) {
            throw new MaterialstoreException(e);
        }
    }

}
