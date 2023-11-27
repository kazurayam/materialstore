package com.kazurayam.materialstore.util;

import com.kazurayam.materialstore.TestOutputOrganizerFactory;
import com.kazurayam.materialstore.core.JobName;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.Store;
import com.kazurayam.unittest.TestOutputOrganizer;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestFixtureUtil {

    private static final TestOutputOrganizer too =
            TestOutputOrganizerFactory.create(TestFixtureUtil.class);
    private static Path resultsDir =
            too.getProjectDir().resolve("src/test/fixtures/sample_results");

    public static void setupFixture(Store store, JobName jobName) throws MaterialstoreException {
        try {
            // make sure the Job directory to be empty
            FileUtils.deleteDirectory(store.getRoot().resolve(jobName.toString()).toFile());
            // stuff the Job directory with a fixture
            Path jobNameDir = store.getRoot().resolve(jobName.toString());
            FileUtils.copyDirectory(resultsDir.toFile(), jobNameDir.toFile());
        } catch (IOException e) {
            throw new MaterialstoreException(e);
        }
    }

}
