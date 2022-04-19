package com.kazurayam.materialstore.materialize;

import com.kazurayam.materialstore.MaterialstoreException;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TargetTest {

    @Test
    public void test_default_locator() throws MaterialstoreException, MalformedURLException {
        Target target =
                new Target.Builder("http://example.com").build();
        assertEquals(new URL("http://example.com"), target.getUrl());
        assertEquals(LocatorType.XPATH, target.getLocatorType());
        assertEquals("/html/body", target.getLocator());
    }

    @Test
    public void test_GoogleSearchPage() throws MalformedURLException, MaterialstoreException {
        Target target =
                new Target.Builder("https://www.google.com")
                        .locatorType(LocatorType.CSS_SELECTOR)
                        .locator("input[name=\"q\"]")
                        .build();
        assertEquals(new URL("https://www.google.com"), target.getUrl());
        assertEquals(LocatorType.CSS_SELECTOR, target.getLocatorType());
        assertEquals("input[name=\"q\"]", target.getLocator());
    }
}
