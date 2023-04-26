package com.kazurayam.materialstore.misc;

import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class URLTest {

    @Test
    public void test_file_URL() throws MalformedURLException {
        Path cwd = Paths.get(System.getProperty("user.dir"));
        Path theFile = cwd.resolve("src/test/java/misc/URLTest.java");
        URL url = theFile.toFile().toURI().toURL();
        System.out.println(url.toString());
        assertEquals("file", url.getProtocol());
        assertEquals("", url.getHost());
        assertEquals(theFile.toString(), url.getFile());
    }
}
