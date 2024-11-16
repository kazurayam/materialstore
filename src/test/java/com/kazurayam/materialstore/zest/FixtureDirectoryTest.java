package com.kazurayam.materialstore.zest;

import com.kazurayam.unittest.TestOutputOrganizer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FixtureDirectoryTest {

    private static TestOutputOrganizer too = TestOutputOrganizerFactory.create(FixtureDirectoryTest.class);

    @Test
    public void test_getFixturesDirectory() {
        Path p = FixtureDirectory.getFixturesDirectory();
        assertNotNull(p);
        assertEquals(p.getFileName().toString(), "fixtures");
        Path alt = too.getProjectDirectory().resolve(FixtureDirectory.FIXTURES_PATH);
        assertEquals(alt, p);
    }

    @Test
    public void test_getPath() {
        FixtureDirectory fixtureDir = new FixtureDirectory("issue#73");
        assertNotNull(fixtureDir.getPath());
        assertEquals(fixtureDir.getPath().getFileName().toString(), "issue#73");
    }

    @Test
    public void test_copyInto() throws IOException {
        Path targetDir = too.resolveClassOutputDirectory();
        FixtureDirectory fixtureDir = new FixtureDirectory("issue#73");
        fixtureDir.copyInto(targetDir);
        Path myAdmin = targetDir.resolve("MyAdmin_visual_inspection_twins");
        assertTrue(Files.exists(myAdmin));
    }
}
