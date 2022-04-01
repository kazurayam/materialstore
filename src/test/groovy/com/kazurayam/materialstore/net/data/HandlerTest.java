package com.kazurayam.materialstore.net.data;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertNull;

public class HandlerTest {

    @Test
    public void test_openConnection() throws IOException {
        DataURLEnabler.enableDataURL();
        Handler handler = new Handler();
        Object o = handler.openConnection(new URL("data:,Hello%2C%20World%21"));
        assertNull(o);
    }

}
