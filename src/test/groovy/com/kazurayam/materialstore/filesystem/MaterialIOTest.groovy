package com.kazurayam.materialstore.filesystem


import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertTrue

class MaterialIOTest {

    private static Path outputDir =
            Paths.get(".").resolve("build/tmp/testOutput")
                    .resolve(MaterialIOTest.class.getName())

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
        String sha1 = MaterialIO.hashJDK(b)
        assertNotNull(sha1)
        println sha1
        assertEquals(40, sha1.length())
    }


    @Test
    void test_getFileName() {
        byte[] bytes = "Hello, world".getBytes("UTF-8")
        ID id = new ID(MaterialIO.hashJDK(bytes))
        MaterialIO mio = new MaterialIO(id, FileType.TXT)
        assertEquals("e02aa1b106d5c7c6a98def2b13005d5b84fd8dc8.txt",
                mio.getFileName())
    }

    @Test
    void test_deserialize_png() {
        Path f = imagesDir.resolve("20210623_225337.development.png")
        byte[] data = MaterialIO.deserialize(f)
        assertTrue(data.length > 0)
    }

    @Test
    void test_serialize_png() {
        Path f = imagesDir.resolve("20210623_225337.development.png")
        byte[] data = MaterialIO.deserialize(f)
        ID id = new ID(MaterialIO.hashJDK(data))
        MaterialIO mio = new MaterialIO(id, FileType.PNG)
        Path work = outputDir.resolve("test_serialize_png")
        Path objectsDir = work.resolve("objects")
        Files.createDirectories(objectsDir)
        Path objectFile = objectsDir.resolve(mio.getFileName())
        mio.serialize(data, objectFile)
    }

    @Test
    void test_deserialize_html() {
        Path f = htmlDir.resolve("development.html")
        byte[] data = MaterialIO.deserialize(f)
        assertTrue(data.length > 0)
    }

    @Test
    void test_serialize_html() {
        Path f = htmlDir.resolve("development.html")
        byte[] data = MaterialIO.deserialize(f)
        ID id = new ID(MaterialIO.hashJDK(data))
        MaterialIO mio = new MaterialIO(id, FileType.HTML)
        Path work = outputDir.resolve("test_serialize_html")
        Path objectsDir = work.resolve("objects")
        Files.createDirectories(objectsDir)
        Path objectFile = objectsDir.resolve(mio.getFileName())
        MaterialIO.serialize(data, objectFile)
    }
}
