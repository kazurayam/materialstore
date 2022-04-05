package com.kazurayam.materialstore.misc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;

public class URLTest {

    @Test
    public void test_getFile() throws MalformedURLException {
        URL url = new URL("https://baeldung.com/articles?topic=java&version=8");
        Assertions.assertEquals("/articles?topic=java&version=8", url.getFile());
    }

    @Test
    public void test_getFile_none() throws MalformedURLException {
        URL url = new URL("https://baeldung.com");
        Assertions.assertEquals("", url.getFile());
    }

    @Test
    public void test_getFile_miminum() throws MalformedURLException {
        URL url = new URL("https://baeldung.com/");
        Assertions.assertEquals("/", url.getFile());
    }

    @Test
    public void test_getFile_with_fragment() throws MalformedURLException {
        URL url = new URL("http://java.sun.com/index.html?param1=value1#chapter1");
        // the fragment "#chapter1" is not technically part of the URL
        Assertions.assertEquals("/index.html?param1=value1", url.getFile());
    }

    @Test
    public void test_getFile_with_fragment2() throws MalformedURLException {
        URL url = new URL("https://katalon-demo-cura.herokuapp.com/#appointment");
        // the fragment "#appointment# is not included in the url.getFile() value
        Assertions.assertEquals("/", url.getFile());
    }

    @Test
    public void test_getPath() throws MalformedURLException {
        URL url = new URL("https://baeldung.com/articles?topic=java&version=8");
        Assertions.assertEquals("/articles", url.getPath());
    }

    @Test
    public void test_getQuery() throws MalformedURLException {
        URL url = new URL("http://baeldung.com/articles?topic=java<em>&version=8</em>");
        Assertions.assertEquals("topic=java<em>&version=8</em>", url.getQuery());
    }

    @Test
    public void test_getHost() throws MalformedURLException {
        URL url = new URL("http://baeldung.com/articles?topic=java<em>&version=8</em>");
        Assertions.assertEquals("baeldung.com", url.getHost());
    }

    @Test
    public void test_getProtocol() throws MalformedURLException {
        URL url = new URL("http://baeldung.com/articles?topic=java<em>&version=8</em>");
        Assertions.assertEquals("http", url.getProtocol());
    }

    @Test
    public void test_getPort() throws MalformedURLException {
        URL url = new URL("http://baeldung.com/articles?topic=java<em>&version=8</em>");
        Assertions.assertEquals(-1, url.getPort());
        Assertions.assertEquals(80, url.getDefaultPort());
        url = new URL("http://baeldung.com:8080/");
        Assertions.assertEquals(8080, url.getPort());
    }

    @Test
    public void test_URL_as_NULL_OBJECT() throws MalformedURLException {
        URL url = new URL("file://null_object");
        Assertions.assertNotNull(url);
    }
}
