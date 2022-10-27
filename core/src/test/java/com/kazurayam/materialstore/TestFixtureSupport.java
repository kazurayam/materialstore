package com.kazurayam.materialstore;

import com.kazurayam.materialstore.filesystem.FileType;
import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.Metadata;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.filesystem.Stores;
import com.kazurayam.materialstore.util.DeleteDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestFixtureSupport {

    private TestFixtureSupport() {}


    public static JobTimestamp create3TXTs(Store store,
                                           JobName jobName,
                                           JobTimestamp jobTimestamp)
            throws MaterialstoreException {
        Material apple = writeTXT(store, jobName, jobTimestamp, "Apple", "01", "it is red");
        Material mikan = writeTXT(store, jobName, jobTimestamp, "Mikan", "02", "it is orange");
        Material money = writeTXT(store, jobName, jobTimestamp, "Money", "03", "it is green");
        return jobTimestamp;
    }

    public static JobTimestamp create3PNGs(Store store,
                                           JobName jobName,
                                           JobTimestamp jobTimestamp)
            throws MaterialstoreException {
        Path fixtures = TestHelper.getFixturesDirectory();
        Material apple = writePNG(store, jobName, jobTimestamp,
                fixtures.resolve("apple_mikan_money/apple.png"), "01", "it is red");
        Material mikan = writePNG(store, jobName, jobTimestamp,
                fixtures.resolve("apple_mikan_money/mikan.png"), "02", "it is orange");
        Material money = writePNG(store, jobName, jobTimestamp,
                fixtures.resolve("apple_mikan_money/money.png"), "03", "it is green");
        return jobTimestamp;
    }

    private static Material writeTXT(Store store,
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

    private static Material writePNG(Store store,
                              JobName jobName,
                              JobTimestamp jobTimestamp,
                              Path png,
                              String step,
                              String label) throws MaterialstoreException {
        Metadata metadata =
                new Metadata.Builder()
                        .put("step", step)
                        .put("label", label).build();
        return store.write(jobName, jobTimestamp, FileType.PNG, metadata, png);
    }

}
