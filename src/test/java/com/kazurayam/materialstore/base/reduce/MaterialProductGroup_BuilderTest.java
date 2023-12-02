package com.kazurayam.materialstore.base.reduce;

import com.kazurayam.materialstore.core.MaterialList;
import com.kazurayam.materialstore.core.MaterialstoreException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MaterialProductGroup_BuilderTest {

    @BeforeAll
    public static void beforeAll() throws IOException {
    }

    @BeforeEach
    public void setup() throws MaterialstoreException {
    }

    @Test
    public void test_ignoreKey_and_ignoreKeys() {
        MaterialList left = MaterialList.NULL_OBJECT;
        MaterialList right = MaterialList.NULL_OBJECT;
        MaterialProductGroup mpg = new MaterialProductGroup.Builder(left, right)
                .ignoreKey("environment")
                .ignoreKeys("URL.host", "URL.protocol")
                .ignoreKeys(Arrays.asList("URL.port", "URL.path"))
                .build();
        assertTrue(mpg.getIgnoreMetadataKeys().contains("environment"));
        assertTrue(mpg.getIgnoreMetadataKeys().contains("URL.host"));
        assertTrue(mpg.getIgnoreMetadataKeys().contains("URL.protocol"));
        assertTrue(mpg.getIgnoreMetadataKeys().contains("URL.port"));
        assertTrue(mpg.getIgnoreMetadataKeys().contains("URL.path"));
        assertFalse(mpg.getIgnoreMetadataKeys().contains("hello"));
    }

}
