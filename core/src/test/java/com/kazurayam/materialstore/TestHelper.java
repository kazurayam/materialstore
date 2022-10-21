package com.kazurayam.materialstore;

import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.filesystem.Stores;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestHelper {

    private static Path currentWorkingDir;
    private static Path testOutputDir;

    static {
        currentWorkingDir = Paths.get(System.getProperty("user.dir"));
        testOutputDir = currentWorkingDir.resolve("build/tmp/testOutput");
    }

    public static void initializeOutputDir() throws IOException {
        if (!Files.exists(testOutputDir)) {
            Files.createDirectories(testOutputDir);
        }
    }

    public static Path getCWD() {
        return getCurrentWorkingDirectory();
    }

    public static Path getCurrentWorkingDirectory() {
        return currentWorkingDir;
    }

    public static Store initializeStore(Object testCase) throws IOException {
        Path root = testOutputDir.resolve(testCase.getClass().getName()).resolve("store");
        if (!Files.exists(root)) {
            Files.createDirectories(root);
        }
        return Stores.newInstance(root);
    }
}
