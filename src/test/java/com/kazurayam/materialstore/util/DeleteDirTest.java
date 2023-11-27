package com.kazurayam.materialstore.util;

import com.kazurayam.materialstore.TestHelper;
import com.kazurayam.materialstore.TestOutputOrganizerFactory;
import com.kazurayam.unittest.TestOutputOrganizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DeleteDirTest {

    private static final TestOutputOrganizer too =
            TestOutputOrganizerFactory.create(DeleteDirTest.class);

    @Test
    public void test_deleteDirectoryRecursively() throws IOException {
        Path sourceDir = TestHelper.getFixturesDirectory().resolve("issue#331");
        Path targetDir = too.getClassOutputDirectory();
        Files.walkFileTree(sourceDir, new CopyDir(sourceDir, targetDir));
        assertTrue(Files.exists(targetDir));
        DeleteDir.deleteDirectoryRecursively(targetDir);
        assertFalse(Files.exists(targetDir));
    }
}
