package com.kazurayam.materialstore;

import com.kazurayam.materialstore.util.DeleteDir;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

public class TestHelperTest {

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
        Path fixtureDir = TestHelper.getFixturesDirectory().resolve("issue#331");
        DeleteDir.deleteDirectoryRecursively(tcSupport.getOutputDir());
        TestHelper.copyDirectory(fixtureDir, tcSupport.getOutputDir());
        TestHelper.copyDirectory(fixtureDir, tcSupport.getOutputDir());
    }
}
