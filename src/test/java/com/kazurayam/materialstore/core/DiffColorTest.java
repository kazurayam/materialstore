package com.kazurayam.materialstore.core;

import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DiffColorTest {

    @Test
    public void test_toRGB_RED() {
        DiffColor dc = new DiffColor(Color.RED);
        assertEquals("#FF0000", dc.toRGB());
    }

    @Test
    public void test_toRGB_GREEN() {
        DiffColor dc = new DiffColor(Color.GREEN);
        assertEquals("#00FF00", dc.toRGB());
    }

    @Test
    public void test_toRGB_BLUE() {
        DiffColor dc = new DiffColor(Color.BLUE);
        assertEquals("#0000FF", dc.toRGB());
    }

}
