package com.kazurayam.materialstore.util;

import com.kazurayam.materialstore.zest.FixtureDirectory;
import com.kazurayam.materialstore.zest.TestOutputOrganizerFactory;
import com.kazurayam.unittest.TestOutputOrganizer;
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
        Path targetDir = too.resolveClassOutputDirectory();
        FixtureDirectory fixtureDir = new FixtureDirectory("issue#331");
        fixtureDir.copyInto(targetDir);
        assertTrue(Files.exists(targetDir));
        too.deleteDir(targetDir);
        assertFalse(Files.exists(targetDir));
    }
}
