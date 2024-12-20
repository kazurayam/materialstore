package com.kazurayam.materialstore.core;

import com.kazurayam.materialstore.zest.TestOutputOrganizerFactory;
import com.kazurayam.materialstore.util.JsonUtil;
import com.kazurayam.unittest.TestOutputOrganizer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;

public class IndexTest {

    private static final TestOutputOrganizer too = TestOutputOrganizerFactory.create(IndexTest.class);
    private static final Path resultsDir = too.getProjectDirectory().resolve("src/test/fixtures/sample_results");
    private final String sampleLine = "6141b40cfe9e7340a483a3097c4f6ff5d20e04ea\tpng\t{\"environment\":\"DevelopmentEnv\", \"URL\":\"http://demoaut-mimic.kazurayam.com/\"}";

    @BeforeAll
    public static void beforeAll() throws IOException {
        too.cleanClassOutputDirectory();
    }

    @Test
    public void test_parseLine_smoke() {
        try {
            IndexEntry indexEntry = IndexEntry.parseLine(sampleLine);
            Assertions.assertNotNull(indexEntry);
        } catch (IllegalArgumentException e) {
            Assertions.fail(e.getMessage());
        }

    }

    @Test
    public void test_formatLine_smoke() {
        ID id = new ID("6141b40cfe9e7340a483a3097c4f6ff5d20e04ea");
        FileType fileType = FileType.PNG;
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>(2);
        map.put("environment", "DevelopmentEnv");
        map.put("URL", "http://demoaut-mimic.kazurayam.com/");
        Metadata metadata = Metadata.builder(map).build();
        IndexEntry indexEntry = new IndexEntry(new MaterialIO(id, fileType), metadata);
        String line = Index.formatLine(indexEntry);
        Assertions.assertEquals(sampleLine, line);
    }

    @Test
    public void test_deserialize() throws MaterialstoreException {
        Path indexFile = Index.getIndexFile(resultsDir.resolve("20210713_093357"));
        Index index = Index.deserialize(indexFile);
        Assertions.assertNotNull(index);
        Assertions.assertEquals(3, index.size());
    }

    @Test
    public void test_serialize() throws MaterialstoreException, IOException {
        Path source = Index.getIndexFile(resultsDir.resolve("20210713_093357"));
        Index index = Index.deserialize(source);
        //
        Path root = too.resolveMethodOutputDirectory("test_serialize");
        Path jobNameDir = root.resolve("myJob");
        Path jobTimestampDir = jobNameDir.resolve(JobTimestamp.now().toString());
        Files.createDirectories(jobTimestampDir);
        Path target = Index.getIndexFile(jobTimestampDir);
        index.serialize(target);
        Assertions.assertTrue(Files.exists(target));
        Assertions.assertTrue(target.toFile().length() > 0);
    }

    @Test
    public void test_toString() throws MaterialstoreException {
        Path source = Index.getIndexFile(resultsDir.resolve("20210713_093357"));
        Index index = Index.deserialize(source);
        String s = index.toString();
        System.out.println(JsonUtil.prettyPrint(s));
    }
}
