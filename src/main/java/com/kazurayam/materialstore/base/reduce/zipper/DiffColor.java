package com.kazurayam.materialstore.base.reduce.zipper;

import java.awt.Color;

/**
 * The color with which different pixels in a diff image to be painted
 */
public class DiffColor {

    public static final DiffColor DEFAULT = new DiffColor(Color.RED);

    private final Color color;

    public DiffColor(Color color) {
        this.color = color;
    }
    public Color getColor() {
        return color;
    }

    /**
     * @return Color.RED -> "#FF0000"
     */
    public String toRGB() {
        return String.format("#%06X", (0xFFFFFF & color.getRGB()));
    }
}
