package com.kazurayam.materialstore.misc

import org.junit.jupiter.api.Test
import static org.junit.jupiter.api.Assertions.assertEquals

class URLTest {

    @Test
    void test_getFile() {
        URL url = new URL("https://baeldung.com/articles?topic=java&version=8");
        assertEquals("/articles?topic=java&version=8", url.getFile());
    }

    @Test
    void test_getFile_none() {
        URL url = new URL("https://baeldung.com");
        assertEquals("", url.getFile());
    }

    @Test
    void test_getFile_miminum() {
        URL url = new URL("https://baeldung.com/");
        assertEquals("/", url.getFile());
    }

    @Test
    void test_getFile_with_fragment() {
        URL url = new URL("http://java.sun.com/index.html?param1=value1#chapter1")
        // the fragment "#chapter1" is not technically part of the URL
        assertEquals("/index.html?param1=value1", url.getFile());
    }

    @Test
    void test_getFile_with_fragment2() {
        URL url = new URL("https://katalon-demo-cura.herokuapp.com/#appointment")
        // the fragment "#appointment# is not included in the url.getFile() value
        assertEquals("/", url.getFile())
    }

    @Test
    void test_getPath() {
        URL url = new URL("https://baeldung.com/articles?topic=java&version=8");
        assertEquals("/articles", url.getPath());
    }

    @Test
    void test_getQuery() {
        URL url = new URL("http://baeldung.com/articles?topic=java<em>&version=8</em>");
        assertEquals("topic=java<em>&version=8</em>", url.getQuery());
    }

    @Test
    void test_getHost() {
        URL url = new URL("http://baeldung.com/articles?topic=java<em>&version=8</em>");
        assertEquals("baeldung.com", url.getHost());
    }

    @Test
    void test_getProtocol() {
        URL url = new URL("http://baeldung.com/articles?topic=java<em>&version=8</em>");
        assertEquals("http", url.getProtocol());
    }

    @Test
    void test_getPort() {
        URL url = new URL("http://baeldung.com/articles?topic=java<em>&version=8</em>");
        assertEquals(-1, url.getPort());
        assertEquals(80, url.getDefaultPort());
        url = new URL("http://baeldung.com:8080/");
        assertEquals(8080, url.getPort());
    }
}
