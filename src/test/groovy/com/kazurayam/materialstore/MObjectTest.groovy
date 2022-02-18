package com.kazurayam.materialstore

import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull

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

    @Disabled
    @Test
    void test_hash() {
        String source = "Hello, world!"
        byte[] b = source.getBytes(StandardCharsets.UTF_8)
        String sha1 = MObject.hash(b)
        assertNotNull(sha1)
        assertEquals(40, sha1.length())
    }

    @Test
    void test_hashJDK() {
        String source = "Hello, world!"
        byte[] b = source.getBytes(StandardCharsets.UTF_8)
        String sha1 = MObject.hashJDK(b)
        assertNotNull(sha1)
        println sha1
        assertEquals(40, sha1.length())
    }

    @Disabled
    @Test
    void test_compare_hash_algo() {
        String source = "Hello, world!"
        byte[] b = source.getBytes(StandardCharsets.UTF_8)
        String sha1 = MObject.hash(b)
        String sha1JDK = MObject.hashJDK(b)
        assertEquals(sha1, sha1JDK)
    }

    @Test
    void test_getFileName() {
        byte[] bytes = "Hello, world".getBytes("UTF-8")
        MObject mObject = new MObject(bytes, FileType.TXT)
        assertEquals("e02aa1b106d5c7c6a98def2b13005d5b84fd8dc8.txt",
                mObject.getFileName())
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
