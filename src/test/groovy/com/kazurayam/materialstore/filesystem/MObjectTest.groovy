package com.kazurayam.materialstore.filesystem


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
import static org.junit.jupiter.api.Assertions.assertTrue

class MObjectTest {

    private static Path outputDir =
            Paths.get(".").resolve("build/tmp/testOutput")
                    .resolve(MObjectTest.class.getName())

    private static Path imagesDir =
            Paths.get(".").resolve("src/test/fixture/sample_images")

    private static Path htmlDir =
            Paths.get(".").resolve("src/test/fixture/sample_html")

    @BeforeAll
    static void beforeAll() {
        if (Files.exists(outputDir)) {
            FileUtils.deleteDirectory(outputDir.toFile())
        }
        Files.createDirectories(outputDir)
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


    @Test
    void test_getFileName() {
        byte[] bytes = "Hello, world".getBytes("UTF-8")
        ID id = new ID(MObject.hashJDK(bytes))
        MObject mObject = new MObject(id, FileType.TXT)
        assertEquals("e02aa1b106d5c7c6a98def2b13005d5b84fd8dc8.txt",
                mObject.getFileName())
    }

    @Test
    void test_deserialize_png() {
        Path f = imagesDir.resolve("20210623_225337.development.png")
        byte[] data = MObject.deserialize(f)
        assertTrue(data.length > 0)
    }

    @Test
    void test_serialize_png() {
        Path f = imagesDir.resolve("20210623_225337.development.png")
        byte[] data = MObject.deserialize(f)
        ID id = new ID(MObject.hashJDK(data))
        MObject mObject = new MObject(id, FileType.PNG)
        Path work = outputDir.resolve("test_serialize_png")
        Path objectsDir = work.resolve("objects")
        Files.createDirectories(objectsDir)
        Path objectFile = objectsDir.resolve(mObject.getFileName())
        mObject.serialize(data, objectFile)
    }

    @Test
    void test_deserialize_html() {
        Path f = htmlDir.resolve("development.html")
        byte[] data = MObject.deserialize(f)
        assertTrue(data.length > 0)
    }

    @Test
    void test_serialize_html() {
        Path f = htmlDir.resolve("development.html")
        byte[] data = MObject.deserialize(f)
        ID id = new ID(MObject.hashJDK(data))
        MObject mObject = new MObject(id, FileType.HTML)
        Path work = outputDir.resolve("test_serialize_html")
        Path objectsDir = work.resolve("objects")
        Files.createDirectories(objectsDir)
        Path objectFile = objectsDir.resolve(mObject.getFileName())
        MObject.serialize(data, objectFile)
    }
}
