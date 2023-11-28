package com.kazurayam.materialstore.zest;

import com.kazurayam.materialstore.core.JobName;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.Store;
import com.kazurayam.unittest.TestOutputOrganizer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class SampleFixtureInjector {

    private static final TestOutputOrganizer too =
            TestOutputOrganizerFactory.create(SampleFixtureInjector.class);

    public static void injectSampleResults(Store store, JobName jobName) throws MaterialstoreException {
        Path resultsDir =
                too.getProjectDir().resolve("src/test/fixtures/sample_results");
        injectSample(resultsDir, store, jobName);
    }

    private static void injectSample(Path sourceFixtureDir, Store store, JobName jobName) throws MaterialstoreException {
        Objects.requireNonNull(sourceFixtureDir);
        assert Files.exists(sourceFixtureDir);
        try {
            Path jobNameDir = store.getRoot().resolve(jobName.toString());
            // make sure the Job directory to be empty
            if (Files.exists(jobNameDir)) {
                too.deleteDir(store.getRoot().resolve(jobName.toString()));
            }
            // stuff the Job directory with a fixture
            too.copyDir(sourceFixtureDir, jobNameDir);
        } catch (IOException e) {
            throw new MaterialstoreException(e);
        }
    }

}
