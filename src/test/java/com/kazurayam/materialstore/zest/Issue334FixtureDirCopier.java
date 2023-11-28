package com.kazurayam.materialstore.zest;

import com.kazurayam.materialstore.core.JobName;
import com.kazurayam.materialstore.core.JobNameNotFoundException;
import com.kazurayam.materialstore.core.JobTimestamp;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.Store;
import com.kazurayam.materialstore.core.Stores;
import com.kazurayam.materialstore.util.DeleteDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue334FixtureDirCopier {

    private Issue334FixtureDirCopier() {}

    public static final Store copyFixtureInto(Path testCaseDir) throws IOException, MaterialstoreException, JobNameNotFoundException {
        if (Files.exists(testCaseDir)) {
            DeleteDir.deleteDirectoryRecursively(testCaseDir);
        }
        Store store = Stores.newInstance(testCaseDir.resolve("store"));
        // Arrange
        FixtureDirectory fixtureDir = new FixtureDirectory("issue#334");
        fixtureDir.copyInto(testCaseDir);
        JobName jobName = new JobName("CURA");
        assertTrue(store.contains(jobName),
                String.format("JobName \"%s\" is not found", jobName));
        List<JobTimestamp> jobTimestampsBeforeCleanUp = store.findAllJobTimestamps(jobName);
        assertEquals(7, jobTimestampsBeforeCleanUp.size());
        return store;
    }
}
