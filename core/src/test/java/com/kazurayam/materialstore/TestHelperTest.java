package com.kazurayam.materialstore;

import com.kazurayam.materialstore.util.DeleteDir;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestHelperTest {

    private Path testClassOutputDir;

    @BeforeEach
    public void beforeEach() throws IOException {
        testClassOutputDir = TestHelper.createTestClassOutputDir(this);
    }

    /**
     * execute TestHelper#copyDirectory(source,destination) twice;
     * should success without errors, such as
     * java.nio.file.DirectoryNotEmptyException
     */
    @Test
    public void test_copyDirectory_overwriting() throws IOException {
        Path fixtureDir = TestHelper.getFixturesDirectory().resolve("issue#331");
        DeleteDir.deleteDirectoryRecursively(testClassOutputDir);
        TestHelper.copyDirectory(fixtureDir, testClassOutputDir);
        TestHelper.copyDirectory(fixtureDir, testClassOutputDir);
        assertTrue(Files.exists(testClassOutputDir.resolve("store/CURA")));
    }
}
