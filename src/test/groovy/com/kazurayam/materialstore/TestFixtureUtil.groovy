package com.kazurayam.materialstore

import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.Store
import org.apache.commons.io.FileUtils

import java.nio.file.Path
import java.nio.file.Paths

class TestFixtureUtil {

    private static Path resultsDir =
            Paths.get(".").resolve("src/test/fixture/sample_results")

    static void setupFixture(Store store, JobName jobName) {
        // make sure the Job directory to be empty
        FileUtils.deleteDirectory(store.getRoot().resolve(jobName.toString()).toFile())
        // stuff the Job directory with a fixture
        Path jobNameDir = store.getRoot().resolve(jobName.toString())
        FileUtils.copyDirectory(resultsDir.toFile(), jobNameDir.toFile())
    }

}
