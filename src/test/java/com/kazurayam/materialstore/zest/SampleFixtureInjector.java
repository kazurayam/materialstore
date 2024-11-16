package com.kazurayam.materialstore.zest;

import com.kazurayam.materialstore.core.FileType;
import com.kazurayam.materialstore.core.JobName;
import com.kazurayam.materialstore.core.JobTimestamp;
import com.kazurayam.materialstore.core.Material;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.Metadata;
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
                too.getProjectDirectory().resolve("src/test/fixtures/sample_results");
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
        Path fixtures = FixtureDirectory.getFixturesDirectory();
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
