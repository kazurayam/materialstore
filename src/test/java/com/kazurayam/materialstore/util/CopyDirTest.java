package com.kazurayam.materialstore.util;

import com.kazurayam.materialstore.zest.FixtureDirectory;
import com.kazurayam.materialstore.zest.TestOutputOrganizerFactory;
import com.kazurayam.unittest.TestOutputOrganizer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CopyDirTest {

    private static final TestOutputOrganizer too =
            TestOutputOrganizerFactory.create(CopyDirTest.class);

    /**
     * execute TestHelper#copyDirectory(source,destination) twice;
     * should success without errors, such as
     * java.nio.file.DirectoryNotEmptyException
     */
    @Test
    public void test_copyDirectory_overwriting() throws IOException {
        Path sourceDir = FixtureDirectory.getFixturesDirectory().resolve("issue#331");
        Path targetDir = too.resolveClassOutputDirectory();
        if (Files.exists(targetDir)) {
            DeleteDir.deleteDirectoryRecursively(targetDir);
        }
        Files.walkFileTree(sourceDir, new CopyDir(sourceDir, targetDir));
        assertTrue(Files.exists(targetDir));
        Files.walkFileTree(sourceDir, new CopyDir(sourceDir, targetDir));
    }
}
