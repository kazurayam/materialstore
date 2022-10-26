package com.kazurayam.materialstore.util;

import com.kazurayam.materialstore.TestCaseSupport;
import com.kazurayam.materialstore.TestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CopyDirTest {

    private TestCaseSupport tcSupport;

    @BeforeEach
    public void beforeEach() throws IOException {
        tcSupport = new TestCaseSupport(this);
    }

    /**
     * execute TestHelper#copyDirectory(source,destination) twice;
     * should success without errors, such as
     * java.nio.file.DirectoryNotEmptyException
     */
    @Test
    public void test_copyDirectory_overwriting() throws IOException {
        Path sourceDir = TestHelper.getFixturesDirectory().resolve("issue#331");
        Path targetDir = tcSupport.getOutputDir();
        DeleteDir.deleteDirectoryRecursively(targetDir);
        Files.walkFileTree(sourceDir, new CopyDir(sourceDir, targetDir));
        Files.walkFileTree(sourceDir, new CopyDir(sourceDir, targetDir));
    }
}
