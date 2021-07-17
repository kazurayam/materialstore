package com.kazurayam.materialstore.store

import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.jupiter.api.Assertions.*

class MObjectTest {

    private static Path outputDir =
            Paths.get(".").resolve("build/tmp/testOutput")
                    .resolve(MObjectTest.class.getName())

    private static Path imagesDir =
            Paths.get(".").resolve("src/test/resources/fixture/sample_images")

    private static Path htmlDir =
            Paths.get(".").resolve("src/test/resources/fixture/sample_html")

    @BeforeAll
    static void beforeAll() {
        if (Files.exists(outputDir)) {
            FileUtils.deleteDirectory(outputDir.toFile())
        }
        Files.createDirectories(outputDir)
    }

    @Test
    void test_deserialize_png() {
        Path f = imagesDir.resolve("20210623_225337.development.png")
        MObject productObject = MObject.deserialize(f, FileType.PNG)
        assertNotNull(productObject)
    }

    @Test
    void test_serialize_png() {
        Path f = imagesDir.resolve("20210623_225337.development.png")
        MObject productObject = MObject.deserialize(f, FileType.PNG)
        Path work = outputDir.resolve("test_serialize_png")
        Path objectsDir = work.resolve("objects")
        Files.createDirectories(objectsDir)
        Path objectFile = objectsDir.resolve(productObject.getFileName())
        productObject.serialize(objectFile)
    }

    @Test
    void test_deserialize_html() {
        Path f = htmlDir.resolve("development.html")
        MObject productObject = MObject.deserialize(f, FileType.HTML)
        assertNotNull(productObject)
    }

    @Test
    void test_serialize_html() {
        Path f = htmlDir.resolve("development.html")
        MObject productObject = MObject.deserialize(f, FileType.HTML)
        Path work = outputDir.resolve("test_serialize_html")
        Path objectsDir = work.resolve("objects")
        Files.createDirectories(objectsDir)
        Path objectFile = objectsDir.resolve(productObject.getFileName())
        productObject.serialize(objectFile)
    }


}
