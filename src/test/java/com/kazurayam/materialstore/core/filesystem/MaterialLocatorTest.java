package com.kazurayam.materialstore.core.filesystem;

import com.kazurayam.materialstore.core.TestHelper;
import com.kazurayam.materialstore.core.util.DeleteDir;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MaterialLocatorTest {

    private Path testClassOutputDir = TestHelper.createTestClassOutputDir(MaterialLocatorTest.class);

    @Test
    public void test_parse_toString() {
        String str = "20221023_144115/07730aaa0c4992dcddb58ef5281faec082c8a8ee";
        MaterialLocator locator = MaterialLocator.parse(str);
        assertEquals(new JobTimestamp("20221023_144115"), locator.getJobTimestamp());
        assertEquals(new ID("07730aaa0c4992dcddb58ef5281faec082c8a8ee"), locator.getID());
        assertEquals(str, locator.toString());
    }

    @Test
    public void test_constructor() throws IOException {
        Path dir = testClassOutputDir.resolve("test_constructor");
        if (Files.exists(dir)) {
            DeleteDir.deleteDirectoryRecursively(dir);
        }
        Files.createDirectories(dir);
        Path root = dir.resolve("store");
        Store store = Stores.newInstance(root);
        //
        String idStr = "6141b40cfe9e7340a483a3097c4f6ff5d20e04ea";
        String sampleLine = idStr + "\tpng\t{}";
        IndexEntry indexEntry = IndexEntry.parseLine(sampleLine);
        JobName jobName = new JobName("foo");
        JobTimestamp jobTimestamp = JobTimestamp.now();
        Material material = new Material(store, jobName, jobTimestamp, indexEntry);
        //
        MaterialLocator locator = new MaterialLocator(material);
        //
        assertEquals(jobTimestamp, locator.getJobTimestamp());
        assertEquals(idStr, locator.getID().toString());
    }

    @Test
    public void test_NULL_OBJECT() {
        MaterialLocator locator = MaterialLocator.NULL_OBJECT;
        assertNotNull(locator);
        assertEquals("_", locator.getJobTimestamp().toString());
    }
}
