package com.kazurayam.materialstore.core;

import com.kazurayam.materialstore.zest.FixtureDirectory;
import com.kazurayam.materialstore.zest.TestOutputOrganizerFactory;
import com.kazurayam.unittest.TestOutputOrganizer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class MaterialIOTest {

    private static final TestOutputOrganizer too = TestOutputOrganizerFactory.create(MaterialIOTest.class);
    private static final Path imagesDir = new FixtureDirectory("sample_images").getPath();
    private static final Path htmlDir = new FixtureDirectory("sample_html").getPath();

    @BeforeAll
    public static void beforeAll() throws IOException {
        too.cleanClassOutputDirectory();
    }

    @Test
    public void test_hashJDK() throws MaterialstoreException {
        String source = "Hello, world!";
        byte[] b = source.getBytes(StandardCharsets.UTF_8);
        String sha1 = MaterialIO.hashJDK(b);
        Assertions.assertNotNull(sha1);
        //System.out.println(sha1);
        Assertions.assertEquals(40, sha1.length());
    }

    @Test
    public void test_getFileName() throws UnsupportedEncodingException {
        byte[] bytes = "Hello, world".getBytes(StandardCharsets.UTF_8);
        ID id = new ID(MaterialIO.hashJDK(bytes));
        MaterialIO mio = new MaterialIO(id, FileType.TXT);
        Assertions.assertEquals("e02aa1b106d5c7c6a98def2b13005d5b84fd8dc8.txt", mio.getFileName());
    }

    @Test
    public void test_deserialize_png() throws MaterialstoreException {
        Path f = imagesDir.resolve("20210623_225337.development.png");
        byte[] data = MaterialIO.deserialize(f);
        Assertions.assertTrue(data.length > 0);
    }

    @Test
    public void test_serialize_png() throws MaterialstoreException, IOException {
        Path f = imagesDir.resolve("20210623_225337.development.png");
        byte[] data = MaterialIO.deserialize(f);
        ID id = new ID(MaterialIO.hashJDK(data));
        MaterialIO mio = new MaterialIO(id, FileType.PNG);
        Path work = too.resolveMethodOutputDirectory("test_serialize_png");
        Path objectsDir = work.resolve("objects");
        Files.createDirectories(objectsDir);
        Path objectFile = objectsDir.resolve(mio.getFileName());
        MaterialIO.serialize(data, objectFile);
    }

    @Test
    public void test_deserialize_html() throws MaterialstoreException {
        Path f = htmlDir.resolve("development.html");
        byte[] data = MaterialIO.deserialize(f);
        Assertions.assertTrue(data.length > 0);
    }

    @Test
    public void test_serialize_html() throws MaterialstoreException, IOException {
        Path f = htmlDir.resolve("development.html");
        byte[] data = MaterialIO.deserialize(f);
        ID id = new ID(MaterialIO.hashJDK(data));
        MaterialIO mio = new MaterialIO(id, FileType.HTML);
        Path work = too.resolveMethodOutputDirectory("test_serialize_html");
        Path objectsDir = work.resolve("objects");
        Files.createDirectories(objectsDir);
        Path objectFile = objectsDir.resolve(mio.getFileName());
        MaterialIO.serialize(data, objectFile);
    }
}
