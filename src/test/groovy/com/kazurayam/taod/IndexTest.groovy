package com.kazurayam.taod

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertTrue
import static org.junit.jupiter.api.Assertions.fail

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class IndexTest {

    private final String sampleLine = """6141b40cfe9e7340a483a3097c4f6ff5d20e04ea\tpng\t["DevelopEnv","http://demoaut-mimic.kazurayam.com/"]"""

    private static Path outputDir =
            Paths.get(".").resolve("build/tmp/testOutput")
                    .resolve(IndexTest.class.getName())

    private static Path resultsDir =
            Paths.get(".").resolve("src/test/resources/fixture/sample_results")

    @Test
    void test_parseLine_smoke() {
        try {
            Tuple t = Index.parseLine(sampleLine)
            assertNotNull(t)
            assertTrue(t[0] instanceof ID)
            assertTrue(t[1] instanceof FileType)
            assertTrue(t[2] instanceof Metadata)
        } catch (IndexParseException e) {
            fail(e.getMessage())
        }
    }

    @Test
    void test_formatLine_smoke() {
        ID id = new ID("6141b40cfe9e7340a483a3097c4f6ff5d20e04ea")
        FileType fileType = FileType.PNG
        Metadata metadata = new Metadata("DevelopEnv", "http://demoaut-mimic.kazurayam.com/")
        Tuple t = new Tuple(id, fileType, metadata)
        String line = Index.formatLine(t)
        assertEquals(sampleLine, line)
    }

    @Test
    void test_deserialize() {
        Path indexFile = Index.getIndexFile(resultsDir.resolve("20210713_093357"))
        Index index = Index.deserialize(indexFile)
        assertNotNull(index)
    }

    @Test
    void test_serialize() {
        Path source = Index.getIndexFile(resultsDir.resolve("20210713_093357"))
        Index index = Index.deserialize(source)
        //
        Path root = outputDir.resolve(".taod")
        Path jobNameDir = root.resolve("test_serialized")
        Path jobResultDir = jobNameDir.resolve(JobTimestamp.now().toString())
        Files.createDirectories(jobResultDir)
        Path indexFile = Index.getIndexFile(jobResultDir)
        index.serialize(indexFile)
        assertTrue(Files.exists(indexFile))
        assertTrue(indexFile.toFile().length() > 0)
    }
}
