package com.kazurayam.materialstore.misc;

import com.kazurayam.materialstore.zest.TestOutputOrganizerFactory;
import com.kazurayam.unittest.TestOutputOrganizer;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class URLTest {

    private static final TestOutputOrganizer too =
            TestOutputOrganizerFactory.create(URLTest.class);

    @Test
    public void test_file_URL() throws MalformedURLException {
        Path theFile = too.getProjectDir()
                .resolve("src/test/java/misc/URLTest.java");
        URL url = theFile.toFile().toURI().toURL();
        System.out.println(url.toString());
        assertEquals("file", url.getProtocol());
        assertEquals("", url.getHost());
        assertEquals(theFile.toString(), url.getFile());
    }
}
