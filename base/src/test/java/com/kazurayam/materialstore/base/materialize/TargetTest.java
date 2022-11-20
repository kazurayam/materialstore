package com.kazurayam.materialstore.base.materialize;

import com.kazurayam.materialstore.core.filesystem.MaterialstoreException;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TargetTest {

    @Test
    public void test_default_By() throws MaterialstoreException, MalformedURLException {
        Target target =
                new Target.Builder("http://example.com").build();
        assertEquals(new URL("http://example.com"), target.getUrl());
        assertEquals("By.xpath: /html/body", target.getHandle().toString());
        System.out.println(target.toJson(true));
    }

    @Test
    public void test_GoogleSearchPage() throws MalformedURLException, MaterialstoreException {
        Target target =
                new Target.Builder("https://www.google.com")
                        .handle(By.cssSelector("input[name=\"q\"]"))
                        .build();
        assertEquals(new URL("https://www.google.com"), target.getUrl());
        assertEquals("By.cssSelector: input[name=\"q\"]", target.getHandle().toString());
    }

    @Test
    public void test_copyWithBy() throws MaterialstoreException {
        Target target =
                new Target.Builder("https://www.google.com")
                        .build();
        Target newTarget = target.copyWith(By.cssSelector("/html/body/section"));
        assertEquals("By.cssSelector: /html/body/section", newTarget.getHandle().toString());
    }

    @Test
    public void test_copyWithAttribute() throws MaterialstoreException {
        Target target =
                new Target.Builder("https://www.google.com")
                        .handle(By.cssSelector("input[name=\"q\"]"))
                        .build();
        Target newTarget = target.copyWith("environment", "Development");
        assertEquals("Development", newTarget.get("environment"));
    }

    @Test
    public void test_copyWithAttributes() throws MaterialstoreException {
        Target target =
                new Target.Builder("https://www.google.com")
                        .handle(By.cssSelector("input[name=\"q\"]"))
                        .build();
        Map<String, String> attributes = Collections.singletonMap("environment", "Development");
        Target newTarget = target.copyWith(attributes);
        assertEquals("Development", newTarget.get("environment"));
    }

    @Test
    public void test_copyConstructor() throws MaterialstoreException {
        Target target =
                new Target.Builder("https://www.google.com")
                        .handle(By.cssSelector("input[name=\"q\"]"))
                        .put("foo", "bar")
                        .build();
        assertEquals("bar", target.get("foo"));
        Target copied = new Target(target);
        assertEquals("bar", copied.get("foo"));
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
