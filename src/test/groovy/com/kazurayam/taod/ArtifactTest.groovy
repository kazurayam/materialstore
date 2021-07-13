package com.kazurayam.taod

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.jupiter.api.Assertions.*

class ArtifactTest {

    private static Path outputDir =
            Paths.get(".").resolve("build/tmp/testOutput")
                    .resolve(ArtifactTest.class.getName())

    private static Path imagesDir =
            Paths.get(".").resolve("src/test/resources/fixture/sample_images")

    private static Path htmlDir =
            Paths.get(".").resolve("src/test/resources/fixture/sample_html")

    @BeforeAll
    static void beforeAll() {
        Files.createDirectories(outputDir)
    }

    @Test
    void test_deserialize_png() {
        Path f = imagesDir.resolve("20210623_225337.develop.png")
        Artifact artifact = Artifact.deserialize(f, FileType.PNG)
        assertNotNull(artifact)
    }

    @Test
    void test_serialize_png() {
        Path f = imagesDir.resolve("20210623_225337.develop.png")
        Artifact artifact = Artifact.deserialize(f, FileType.PNG)
        Path work = outputDir.resolve("test_serialize_png")
        Path artifactsDir = work.resolve("artifacts")
        Files.createDirectories(artifactsDir)
        Path artifactFile = artifactsDir.resolve(artifact.getFileName())
        artifact.serialize(artifactFile)
    }

    @Test
    void test_deserialize_html() {
        Path f = htmlDir.resolve("develop.html")
        Artifact artifact = Artifact.deserialize(f, FileType.HTML)
        assertNotNull(artifact)
    }

    @Test
    void test_serialize_html() {
        Path f = htmlDir.resolve("develop.html")
        Artifact artifact = Artifact.deserialize(f, FileType.HTML)
        Path work = outputDir.resolve("test_serialize_html")
        Path artifactsDir = work.resolve("artifacts")
        Files.createDirectories(artifactsDir)
        Path artifactFile = artifactsDir.resolve(artifact.getFileName())
        artifact.serialize(artifactFile)
    }


}
