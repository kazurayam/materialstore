package com.kazurayam.materialstore.core.util;

import com.kazurayam.materialstore.core.TestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CopyDirTest {

    private Path testClassOutputDir;

    @BeforeEach
    public void beforeEach() throws IOException {
        testClassOutputDir = TestHelper.createTestClassOutputDir(CopyDirTest.class);
    }

    /**
     * execute TestHelper#copyDirectory(source,destination) twice;
     * should success without errors, such as
     * java.nio.file.DirectoryNotEmptyException
     */
    @Test
    public void test_copyDirectory_overwriting() throws IOException {
        Path sourceDir = TestHelper.getFixturesDirectory().resolve("issue#331");
        Path targetDir = testClassOutputDir;
        DeleteDir.deleteDirectoryRecursively(targetDir);
        Files.walkFileTree(sourceDir, new CopyDir(sourceDir, targetDir));
        assertTrue(Files.exists(targetDir));
        Files.walkFileTree(sourceDir, new CopyDir(sourceDir, targetDir));
    }
}
