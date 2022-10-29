package com.kazurayam.materialstore.core.filesystem;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FilenameTest {

    @Test
    public void test_http() {
        assertTrue(Filename.isValid("http"));
    }

    @Test
    public void test_empty() {
        assertFalse(Filename.isValid(""));
    }

    @Test
    public void test_requireNonNull() {
        NullPointerException npe = Assertions.assertThrows(NullPointerException.class, () -> {
            Filename.isValid(null);
        });
        Assertions.assertEquals("argument s must not be null", npe.getMessage());
    }
}
