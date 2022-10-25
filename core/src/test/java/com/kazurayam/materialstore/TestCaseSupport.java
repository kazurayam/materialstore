package com.kazurayam.materialstore;

import com.kazurayam.materialstore.filesystem.FileType;
import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.Metadata;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.filesystem.Stores;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class TestCaseSupport {

    private final Object testCase;
    private final Path outputDir;
    private final Path root;
    private final Store store;

    public TestCaseSupport(Object testCase) {
        this.testCase = testCase;
        // create the out directory for the testCase object to write output files
        outputDir = TestHelper.getTestOutputDir()
                .resolve(testCase.getClass().getName());
        try {
            if (!Files.exists(outputDir)) {
                Files.createDirectories(outputDir);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        root = outputDir.resolve("store");
        store = Stores.newInstance(root);
    }

    public Path getOutputDir() {
        return outputDir;
    }

    public Store getStore() {
        return store;
    }

    public int deleteJobName(JobName jobName) throws MaterialstoreException, IOException {
        if (store.contains(jobName)) {
            return store.deleteJobName(jobName);
        }
        return 0;
    }

    /**
     * #331
     * @return
     */
    public void copyFixture(Path fixtureDir, Path testCaseOutputDir)
            throws MaterialstoreException, IOException {
        Objects.requireNonNull(fixtureDir);
        if (!Files.exists(fixtureDir)) {
            throw new MaterialstoreException(String.format("%s is not present", fixtureDir));
        }
        Objects.requireNonNull(testCaseOutputDir);
        TestHelper.copyDirectory(fixtureDir, testCaseOutputDir);
    }

    public JobTimestamp create3TXTsWithStepAndLabel(JobName jobName, JobTimestamp jobTimestamp)
            throws MaterialstoreException {
        Material apple = writeTXT(jobName, jobTimestamp, "Apple", "01", "it is red");
        Material mikan = writeTXT(jobName, jobTimestamp, "Mikan", "02", "it is orange");
        Material money = writeTXT(jobName, jobTimestamp, "Money", "03", "it is green");
        return jobTimestamp;
    }

    public JobTimestamp create3PNGsWithStepAndLabel(JobName jobName, JobTimestamp jobTimestamp)
            throws MaterialstoreException {
        Path fixtures = TestHelper.getFixturesDirectory();
        Material apple = writePNG(jobName, jobTimestamp,
                fixtures.resolve("apple_mikan_money/apple.png"), "01", "it is red");
        Material mikan = writePNG(jobName, jobTimestamp,
                fixtures.resolve("apple_mikan_money/mikan.png"), "02", "it is orange");
        Material money = writePNG(jobName, jobTimestamp,
                fixtures.resolve("apple_mikan_money/money.png"), "03", "it is green");
        return jobTimestamp;
    }

    private Material writeTXT(JobName jobName,
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

    private Material writePNG(JobName jobName,
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
