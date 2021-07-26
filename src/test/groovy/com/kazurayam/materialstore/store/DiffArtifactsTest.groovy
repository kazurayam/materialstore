package com.kazurayam.materialstore.store

import com.kazurayam.materialstore.TestFixtureUtil
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

class DiffArtifactsTest {

    private static Path outputDir =
            Paths.get(".").resolve("build/tmp/testOutput")
                    .resolve(DiffArtifactsTest.class.getName())

    private static Path resultsDir =
            Paths.get(".").resolve("src/test/resources/fixture/sample_results")

    private DiffArtifacts diffArtifacts
    private DiffArtifact nullEntry

    @BeforeAll
    static void beforeAll() {
        if (Files.exists(outputDir)) {
            FileUtils.deleteDirectory(outputDir.toFile())
        }
        Files.createDirectories(outputDir)
    }

    @BeforeEach
    void before() {
        diffArtifacts = new DiffArtifacts()
    }

    @Test
    void test_add_size_get() {
        diffArtifacts.add(DiffArtifact.NULL_OBJECT)
        assertEquals(1, diffArtifacts.size())
        assertEquals(DiffArtifact.NULL_OBJECT, diffArtifacts.get(0))
    }

    @Test
    void test_iterator() {
        diffArtifacts.add(DiffArtifact.NULL_OBJECT)
        diffArtifacts.each { DiffArtifact it ->
            assert it == DiffArtifact.NULL_OBJECT
        }
    }

    /*
    [{"expected":"{\"jobName\":\"_\",\"jobTimestamp\":\"_\",\"ID\":\"0000000000000000000000000000000000000000\",\"fileType\":\"\",\"metadata\":\"{}\"}","actual":"{\"jobName\":\"_\",\"jobTimestamp\":\"_\",\"ID\":\"0000000000000000000000000000000000000000\",\"fileType\":\"\",\"metadata\":\"{}\"}","diff":"{\"jobName\":\"_\",\"jobTimestamp\":\"_\",\"ID\":\"0000000000000000000000000000000000000000\",\"fileType\":\"\",\"metadata\":\"{}\"}","descriptor":"{}","diffRatio":0.0}]
     */
    @Test
    void test_toString() {
        diffArtifacts.add(DiffArtifact.NULL_OBJECT)
        String s = diffArtifacts.toString()
        println s
        assertTrue(s.contains("expected"), s)
        assertTrue(s.contains("actual"), s)
        assertTrue(s.contains("diff"), s)
        assertTrue(s.contains("descriptor"), s)
        assertTrue(s.contains("diffRatio"), s)
    }

    @Test
    void test_countWarnings() {
        DiffArtifact tmp = new DiffArtifact(Material.NULL_OBJECT,
                Material.NULL_OBJECT, MetadataPattern.NULL_OBJECT)
        tmp.setDiffRatio(45.0d)
        diffArtifacts.add(tmp)
        assertEquals(1, diffArtifacts.countWarnings(0.00d))
        assertEquals(0, diffArtifacts.countWarnings(45.00d))
        assertEquals(0, diffArtifacts.countWarnings(45.01d))
    }

}