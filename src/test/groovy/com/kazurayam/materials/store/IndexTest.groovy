package com.kazurayam.materials.store


import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertTrue
import static org.junit.jupiter.api.Assertions.fail

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class IndexTest {

    private final String sampleLine = """6141b40cfe9e7340a483a3097c4f6ff5d20e04ea\tpng\t{"URL":"http://demoaut-mimic.kazurayam.com/","profile":"DevelopmentEnv"}"""

    private static Path outputDir =
            Paths.get(".").resolve("build/tmp/testOutput")
                    .resolve(IndexTest.class.getName())

    private static Path resultsDir =
            Paths.get(".").resolve("src/test/resources/fixture/sample_results")

    @Test
    void test_parseLine_smoke() {
        try {
            IndexEntry indexEntry = IndexEntry.parseLine(sampleLine)
            assertNotNull(indexEntry)
        } catch (IllegalArgumentException e) {
            fail(e.getMessage())
        }
    }

    @Test
    void test_formatLine_smoke() {
        ID id = new ID("6141b40cfe9e7340a483a3097c4f6ff5d20e04ea")
        FileType fileType = FileType.PNG
        Metadata metadata = new Metadata(["profile": "DevelopmentEnv", "URL": "http://demoaut-mimic.kazurayam.com/"])
        IndexEntry indexEntry = new IndexEntry(id, fileType, metadata)
        String line = Index.formatLine(indexEntry)
        assertEquals(sampleLine, line)
    }

    @Test
    void test_deserialize() {
        Path indexFile = Index.getIndexFile(resultsDir.resolve("20210713_093357"))
        Index index = Index.deserialize(indexFile)
        assertNotNull(index)
        assertEquals(3, index.size())
    }

    @Test
    void test_serialize() {
        Path source = Index.getIndexFile(resultsDir.resolve("20210713_093357"))
        Index index = Index.deserialize(source)
        //
        Path root = outputDir.resolve("Materials")
        Path jobNameDir = root.resolve("test_serialized")
        Path jobTimestampDir = jobNameDir.resolve(JobTimestamp.now().toString())
        Files.createDirectories(jobTimestampDir)
        Path target = Index.getIndexFile(jobTimestampDir)
        index.serialize(target)
        assertTrue(Files.exists(target))
        assertTrue(target.toFile().length() > 0)
    }
}
