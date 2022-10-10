package com.kazurayam.materialstore.filesystem.metadata;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MetadataDescriptionTest {

    @Test
    public void test_smoke() {
        String representation = "{\"timestamp\":\"20221010-091203\", \"step\":\"01\"}";
        MetadataDescription md = new MetadataDescription(representation);
        assertEquals(representation, md.getRepresentation());
        assertEquals(representation, md.toString());
    }
}
