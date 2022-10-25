package com.kazurayam.materialstore;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Objects;

public class TestHelper {
    private static final Path currentWorkingDir;
    private static final Path testOutputDir;
    private static final Path fixturesDir;

    static {
        currentWorkingDir = Paths.get(System.getProperty("user.dir"));
        testOutputDir = currentWorkingDir.resolve("build/tmp/testOutput");
        fixturesDir = currentWorkingDir.resolve("src/test/fixtures");
    }

    public static Path getCurrentWorkingDirectory() {
        return currentWorkingDir;
    }

    public static Path getCWD() {
        return getCurrentWorkingDirectory();
    }

    public static Path getFixturesDirectory() {
        return fixturesDir;
    }

    public static Path getTestOutputDir() {
        return testOutputDir;
    }

    /**
     * Create dir if not exits.
     * Delete dir if already exits, and recreate it.
     * @param dir
     * @return
     * @throws IOException
     */
    public static Path initializeDirectory(Path dir) throws IOException {
        if (Files.exists(dir)) {
            TestHelper.deleteDirectoryRecursively(dir);
        }
        Files.createDirectories(dir);
        return dir;
    }

    public static void deleteDirectoryRecursively(Path dir) throws IOException {
        Objects.requireNonNull(dir);
        if (!Files.exists(dir)) {
            throw new IOException(dir.toString() + " does not exist");
        }
        Files.walk(dir)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

}
