package com.kazurayam.materialstore.zest;

import com.kazurayam.unittest.TestOutputOrganizer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class FixtureDirectory {

    private static TestOutputOrganizer too =
            new TestOutputOrganizer.Builder(FixtureDirectory.class)
                    .build();
    public static final String FIXTURES_PATH = "src/test/fixtures";
    private Path dir;

    public FixtureDirectory(String directoryName) {
        Objects.requireNonNull(directoryName);
        this.dir = getFixturesDirectory().resolve(directoryName);
        if (!Files.exists(dir)) {
            throw new IllegalArgumentException(dir + " does not exist");
        }
    }

    public static Path getFixturesDirectory() {
        Path projectDir = too.getProjectDirectory();
        return projectDir.resolve(FIXTURES_PATH);
    }

    public Path getPath() {
        return this.dir;
    }

    public void copyInto(Path targetDir) throws IOException {
        too.copyDir(this.dir, targetDir);
    }

    public String toString() {
        return this.dir.toString();
    }
}
