package com.kazurayam.materialstore;

import com.kazurayam.materialstore.util.CopyDir;
import com.kazurayam.materialstore.util.DeleteDir;
import com.kazurayam.unittest.TestOutputOrganizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class TestHelper {
    private static final Logger logger = LoggerFactory.getLogger(TestHelper.class);
    private static final TestOutputOrganizer too =
            TestOutputOrganizerFactory.create(TestHelper.class);
    private static final Path testOutputDir;
    private static final Path fixturesDir;

    static {
        testOutputDir = too.getProjectDir().resolve("build/tmp/testOutput");
        fixturesDir = too.getProjectDir().resolve("src/test/fixtures");
    }

    public static Path getFixturesDirectory() {
        return fixturesDir;
    }

    public static Path getTestOutputDir() {
        return testOutputDir;
    }

    public static void copyDirectory(Path sourceDir, Path targetDir) throws IOException {
        Objects.requireNonNull(sourceDir);
        Objects.requireNonNull(targetDir);
        if (!Files.exists(sourceDir)) {
            throw new IOException(String.format("%s does not exist", sourceDir));
        }
        if (!Files.isDirectory(sourceDir)) {
            throw new IOException(String.format("%s is not a directory", sourceDir));
        }
        if (!Files.exists(targetDir.getParent())) {
            Files.createDirectories(targetDir.getParent());
        }
        Files.walkFileTree(sourceDir, new CopyDir(sourceDir, targetDir));
    }

    /**
     * create the out directory for the testCase object to write output files
     */
    public static Path createTestClassOutputDir(Class<?> clazz) {
        Objects.requireNonNull(clazz);
        Path output = getTestOutputDir()
                .resolve(clazz.getName());
        try {
            if (!Files.exists(output)) {
                Files.createDirectories(output);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return output;
    }
}
