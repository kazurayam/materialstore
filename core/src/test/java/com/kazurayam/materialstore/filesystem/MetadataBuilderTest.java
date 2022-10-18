package com.kazurayam.materialstore.filesystem;

import com.kazurayam.materialstore.filesystem.Metadata;
import com.kazurayam.materialstore.util.KeyValuePair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MetadataBuilderTest {

    private static final String fixture = "https://www.google.com/search?q=katalon&source=hp&ei=TkZLY9meB6O12roPw5-SmAU&iflsig=AJiK0e8AAAAAY0tUXrwBwDKRE8CXoRWoDezm2LZ9PhlV&ved=0ahUKEwjZ_ZXMteP6AhWjmlYBHcOPBFMQ4dUDCAk&uact=5&oq=katalon&gs_lcp=Cgdnd3Mtd2l6EAMyBQgAEIAEMgUIABCABDIFCAAQgAQyBQgAEIAEMgUIABCABDIFCAAQgAQyBQgAEIAEMgUIABCABDIFCAAQgAQyBQgAEIAEOg0IABAEEIAEELEDEIMBOgcIABAEEIAEOgcIABCABBAKUABYIGD2G2gAcAB4AIABxAGIAaAFkgEDMS4zmAEAoAEB&sclient=gws-wiz#main";
    private static Metadata metadata;

    @BeforeAll
    public static void beforeAll() throws MalformedURLException {
        URL url = new URL(fixture);
        metadata = new Metadata.Builder(url).build();
    }

    @Test
    public void testURLProtocol() {
        assertEquals("https", metadata.get("URL.protocol"));
    }

    @Test
    public void testURLHost() {
        assertEquals("www.google.com", metadata.get("URL.host"));
    }

    @Test
    public void testURLPort() {
        assertEquals("80", metadata.get("URL.port"));
    }

    @Test
    public void testURLPath() {
        assertEquals("/search", metadata.get("URL.path"));
    }

    @Test
    public void testURLFragment() {
        assertEquals("main", metadata.get("URL.fragment"));
    }

    @Test
    public void testURLQuery() {
        assertEquals("katalon", metadata.get("URL.query?q"));
        assertEquals("hp", metadata.get("URL.query?source"));
        assertEquals("TkZLY9meB6O12roPw5-SmAU", metadata.get("URL.query?ei"));
        assertEquals("AJiK0e8AAAAAY0tUXrwBwDKRE8CXoRWoDezm2LZ9PhlV", metadata.get("URL.query?iflsig"));
        assertEquals("0ahUKEwjZ_ZXMteP6AhWjmlYBHcOPBFMQ4dUDCAk", metadata.get("URL.query?ved"));
        assertEquals("5", metadata.get("URL.query?uact"));
        assertEquals("katalon", metadata.get("URL.query?oq"));
        assertEquals("Cgdnd3Mtd2l6EAMyBQgAEIAEMgUIABCABDIFCAAQgAQyBQgAEIAEMgUIABCABDIFCAAQgAQyBQgAEIAEMgUIABCABDIFCAAQgAQyBQgAEIAEOg0IABAEEIAEELEDEIMBOgcIABAEEIAEOgcIABCABBAKUABYIGD2G2gAcAB4AIABxAGIAaAFkgEDMS4zmAEAoAEB",
                metadata.get("URL.query?gs_lcp"));
        assertEquals("gws-wiz", metadata.get("URL.query?sclient"));
    }

    @Test
    public void testExclude() throws MalformedURLException {
        URL url0 = new URL(fixture);
        Metadata md = new Metadata.Builder(url0)
                .exclude("URL.query?iflsig", "URL.query?gs_lcp").build();
        assertTrue(md.containsKey("URL.host"));
        assertTrue(md.containsKey("URL.query?q"));
        assertFalse(md.containsKey("URL.query?iflsig"));
        assertFalse(md.containsKey("URL.query?gs_lcp"));
        //System.out.println("[MetadataBuilderTest#testExclude] md=" + md.toJson(true));
    }

    @Test
    public void testFileURLSupport() throws MalformedURLException {
        String fileUrlString = "file:/Users/who/bar/baz.txt";
        URL url = new URL(fileUrlString);
        Metadata md = new Metadata.Builder(url).build();
        System.out.println(md.toJson(true));
        assertTrue(md.containsKey("URL.protocol"), "URL.protocol is not contained");
        assertEquals("file", md.get("URL.protocol"));
        assertTrue(md.containsKey("URL.path"), "URL.path is not contained");
        assertEquals("/Users/who/bar/baz.txt", md.get("URL.path"));
    }
}
