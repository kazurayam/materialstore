package com.kazurayam.materialstore.util;

import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.Store;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestFixtureUtil {

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

    private static Path resultsDir = Paths.get(".").resolve("src/test/fixture/sample_results");
}
