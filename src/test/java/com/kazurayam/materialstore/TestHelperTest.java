package com.kazurayam.materialstore;

import com.kazurayam.materialstore.util.DeleteDir;
import com.kazurayam.unittest.TestOutputOrganizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestHelperTest {

    private static final TestOutputOrganizer too =
            TestOutputOrganizerFactory.create(TestHelperTest.class);

    /**
     * execute TestHelper#copyDirectory(source,destination) twice;
     * should success without errors, such as
     * java.nio.file.DirectoryNotEmptyException
     */
    @Test
    public void test_copyDirectory_overwriting() throws IOException {
        Path classOutputDir = too.getClassOutputDirectory();
        Path fixtureDir = TestHelper.getFixturesDirectory().resolve("issue#331");
        DeleteDir.deleteDirectoryRecursively(classOutputDir);
        TestHelper.copyDirectory(fixtureDir, classOutputDir);
        TestHelper.copyDirectory(fixtureDir, classOutputDir);
        assertTrue(Files.exists(classOutputDir.resolve("store/CURA")));
    }
}
