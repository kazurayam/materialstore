package com.kazurayam.materialstore.filesystem.metadata;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PlainDescriptionTest {

    @Test
    public void test_smoke() {
        String representation = "{\"timestamp\":\"20221010-091203\", \"step\":\"01\"}";
        PlainDescription pd = new PlainDescription(representation);
        assertEquals(representation, pd.getRepresentation());
        assertEquals(representation, pd.toString());
    }
}
