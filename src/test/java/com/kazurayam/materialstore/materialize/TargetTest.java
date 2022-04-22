package com.kazurayam.materialstore.materialize;

import com.kazurayam.materialstore.MaterialstoreException;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TargetTest {

    @Test
    public void test_default_By() throws MaterialstoreException, MalformedURLException {
        Target target =
                new Target.Builder("http://example.com").build();
        assertEquals(new URL("http://example.com"), target.getUrl());
        assertEquals("By.xpath: /html/body", target.getBy().toString());
        System.out.println(target.toJson(true));
    }

    @Test
    public void test_GoogleSearchPage() throws MalformedURLException, MaterialstoreException {
        Target target =
                new Target.Builder("https://www.google.com")
                        .by(By.cssSelector("input[name=\"q\"]"))
                        .build();
        assertEquals(new URL("https://www.google.com"), target.getUrl());
        assertEquals("By.cssSelector: input[name=\"q\"]", target.getBy().toString());
    }

    @Test
    public void test_put() throws MaterialstoreException {
        Target target =
                new Target.Builder("https://www.google.com")
                        .by(By.cssSelector("input[name=\"q\"]"))
                        .build();
        target.put("profile", "Development");
        assertEquals("Development", target.get("profile"));
    }

    @Test
    public void test_putAll() throws MaterialstoreException {
        Target target =
                new Target.Builder("https://www.google.com")
                        .by(By.cssSelector("input[name=\"q\"]"))
                        .build();
        Map<String, String> attributes = Collections.singletonMap("profile", "Development");
        target.putAll(attributes);
        assertEquals("Development", target.get("profile"));
    }

    @Test
    public void test_copyConstructor() throws MaterialstoreException {
        Target target =
                new Target.Builder("https://www.google.com")
                        .by(By.cssSelector("input[name=\"q\"]"))
                        .build();
        Map<String, String> attributes = Collections.singletonMap("profile", "Development");
        target.putAll(attributes);
        assertNull(target.get("foo"));
        Target copied = new Target(target);
        copied.put("foo", "bar");
        assertEquals("Development", copied.get("profile"));
        assertEquals("bar", copied.get("foo"));
        assertNull(target.get("foo"));
    }

    @Test
    public void test_builder_URL() throws MalformedURLException {
        URL url = new URL("http://www.example.com");
        Target target = Target.builder(url).build();
        assertNotNull(target);
    }

    @Test
    public void test_builder_String() throws MaterialstoreException {
        String urlString = "http://www.example.com";
        Target target = Target.builder(urlString).build();
        assertNotNull(target);
    }
}
