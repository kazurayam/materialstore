package com.kazurayam.materialstore

import com.kazurayam.materialstore.store.JobName
import com.kazurayam.materialstore.store.JobTimestamp
import com.kazurayam.materialstore.store.Jobber
import com.kazurayam.materialstore.store.StoreImpl
import org.apache.commons.io.FileUtils

import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.jupiter.api.Assertions.*

class TestFixtureUtil {

    private static Path resultsDir =
            Paths.get(".").resolve("src/test/resources/fixture/sample_results")

    static void setupFixture(StoreImpl storeImpl, JobName jobName) {
        // make sure the Job directory to be empty
        FileUtils.deleteDirectory(storeImpl.getRoot().resolve(jobName.toString()).toFile())
        // stuff the Job directory with a fixture
        Path jobNameDir = storeImpl.getRoot().resolve(jobName.toString())
        FileUtils.copyDirectory(resultsDir.toFile(), jobNameDir.toFile())
    }

}