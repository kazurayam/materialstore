package com.kazurayam.materialstore.filesystem.metadata;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IgnoreMetadataKeysTest {
    @Test
    public void test_of() {
        IgnoreMetadataKeys ignoreMetadataKeys =
                new IgnoreMetadataKeys.Builder()
                        .ignoreKeys("profile", "URL.hostname").build();
        Assertions.assertNotNull(ignoreMetadataKeys);
        Assertions.assertTrue(ignoreMetadataKeys.contains("profile"));
        Assertions.assertEquals(2, ignoreMetadataKeys.size());
    }

    @Test
    public void test_toString() {
        IgnoreMetadataKeys ignoreMetadataKeys =
                new IgnoreMetadataKeys.Builder()
                        .ignoreKeys("profile", "URL.hostname").build();
        String str = ignoreMetadataKeys.toString();
        Assertions.assertNotNull(str);
        Assertions.assertEquals("[\"URL.hostname\", \"profile\"]", str);
    }

}
