package com.kazurayam.materialstore.base;

import com.kazurayam.materialstore.core.TestHelper;
import com.kazurayam.materialstore.core.filesystem.JobName;
import com.kazurayam.materialstore.core.filesystem.JobTimestamp;
import com.kazurayam.materialstore.core.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.core.filesystem.Store;
import com.kazurayam.materialstore.core.filesystem.Stores;
import com.kazurayam.materialstore.core.util.DeleteDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FixtureDirCopier {

    private FixtureDirCopier() {}

    public static final Store copyIssue334FixtureInto(Path testCaseDir) throws IOException, MaterialstoreException {
        if (Files.exists(testCaseDir)) {
            DeleteDir.deleteDirectoryRecursively(testCaseDir);
        }
        Store store = Stores.newInstance(testCaseDir.resolve("store"));
        // Arrange
        Path fixtureDir = TestHelper.getFixturesDirectory().resolve("issue#334");
        TestHelper.copyDirectory(fixtureDir, testCaseDir);
        JobName jobName = new JobName("CURA");
        assertTrue(store.contains(jobName),
                String.format("JobName \"%s\" is not found", jobName));
        List<JobTimestamp> jobTimestampsBeforeCleanUp = store.findAllJobTimestamps(jobName);
        assertEquals(7, jobTimestampsBeforeCleanUp.size());
        return store;
    }
}
