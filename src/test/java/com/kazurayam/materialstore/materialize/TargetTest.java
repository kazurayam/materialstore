package com.kazurayam.materialstore.materialize;

import com.kazurayam.materialstore.MaterialstoreException;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

}
