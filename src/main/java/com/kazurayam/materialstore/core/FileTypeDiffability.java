package com.kazurayam.materialstore.core;

/**
 * Diff-ability
 * <p>
 * MimeType "text/html" is a simple text, so it is diffable by TextDifferToHTML
 * MimeType "image/png" is an image, so it is diffable by ImageDifferToPNG
 * MimeType "font/woff2" is an unknown, so it is unable to create a diff info.
 */
public enum FileTypeDiffability {
    AS_TEXT(true),
    AS_IMAGE(true),
    UNABLE(false);

    private final Boolean diffability;
    FileTypeDiffability(Boolean diffability) {
        this.diffability = diffability;
    }
    public Boolean isDiffable() {
        return this.diffability;
    }
}
