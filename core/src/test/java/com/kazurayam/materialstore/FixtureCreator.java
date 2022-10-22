package com.kazurayam.materialstore;

import com.kazurayam.materialstore.filesystem.FileType;
import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.Metadata;
import com.kazurayam.materialstore.filesystem.Store;

import java.io.IOException;

public class FixtureCreator {

    /**
     *
     * @param store
     * @param jobName
     */
    public static JobTimestamp createFixtures(Store store, JobName jobName, JobTimestamp base) throws MaterialstoreException, IOException {
        JobTimestamp jobTimestamp = JobTimestamp.laterThan(base);
        Material apple = writeTXTFixtureIntoStore(store, jobName, jobTimestamp, "Apple", "01", "it is red");
        Material orange = writeTXTFixtureIntoStore(store, jobName, jobTimestamp, "Orange", "02", "it is orange");
        Material money = writeTXTFixtureIntoStore(store, jobName, jobTimestamp, "Money", "03", "it is green");
        return jobTimestamp;
    }

    /**
     *
     */
    public static Material writeTXTFixtureIntoStore(Store store,
                                                    JobName jobName,
                                                    JobTimestamp jobTimestamp,
                                                    String text,
                                                    String step,
                                                    String label) throws MaterialstoreException {
        Metadata metadata =
                new Metadata.Builder()
                        .put("step", step)
                        .put("label", label).build();
        return store.write(jobName, jobTimestamp, FileType.TXT, metadata, text);
    }
}
