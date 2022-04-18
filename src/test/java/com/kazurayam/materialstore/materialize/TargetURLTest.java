package com.kazurayam.materialstore.materialize;

import com.kazurayam.materialstore.MaterialstoreException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TargetURLTest {

    @Test
    public void test_default_locator() throws MaterialstoreException, MalformedURLException {
        TargetURL targetUrl =
                new TargetURL.Builder("http://example.com").build();
        assertEquals(new URL("http://example.com"), targetUrl.getUrl());
        assertEquals(LocatorType.XPATH, targetUrl.getLocatorType());
        assertEquals("/html/body", targetUrl.getLocator());
    }

    @Test
    public void test_GoogleSearchPage() throws MalformedURLException, MaterialstoreException {
        TargetURL targetUrl =
                new TargetURL.Builder("https://www.google.com")
                        .locatorType(LocatorType.CSS_SELECTOR)
                        .locator("input[name=\"q\"]")
                        .build();
        assertEquals(new URL("https://www.google.com"), targetUrl.getUrl());
        assertEquals(LocatorType.CSS_SELECTOR, targetUrl.getLocatorType());
        assertEquals("input[name=\"q\"]", targetUrl.getLocator());
    }
}
