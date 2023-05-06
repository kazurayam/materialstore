package com.kazurayam.materialstore.core;

import java.util.Arrays;
import java.util.List;

public enum FileType implements IFileType {

    BMP  ("bmp", FileTypeDiffability.AS_IMAGE, Arrays.asList("image/bmp")),

    /**
     * Cascading Style Sheet
     */
    CSS  ("css", FileTypeDiffability.AS_TEXT,  Arrays.asList("text/css")),

    /**
     * Comma Separated Values
     */
    CSV  ("csv", FileTypeDiffability.AS_TEXT,  Arrays.asList("text/csv", "text/plain")),

    /**
     * Microsoft Word document file
     */
    DOC  ("doc", FileTypeDiffability.UNABLE,   Arrays.asList("application/msword")),
    DOCX ("docx",FileTypeDiffability.UNABLE,   Arrays.asList("application/vnd.openxmlformats-officedocument.wordprocessingml.document")),

    /**
     * Graphviz DOT file
     */
    DOT  ("dot", FileTypeDiffability.AS_TEXT,  Arrays.asList("text/plain")),
    GIF  ("gif", FileTypeDiffability.AS_IMAGE, Arrays.asList("image/gif")),

    /**
     * ".html"
     */
    HTML ("html",FileTypeDiffability.AS_TEXT,  Arrays.asList("text/html")),
    JAR  ("jar", FileTypeDiffability.UNABLE,   Arrays.asList("application/java-archive")),
    JPG  ("jpg", FileTypeDiffability.AS_IMAGE, Arrays.asList("image/jpeg")),
    JPEG ("jpeg",FileTypeDiffability.AS_IMAGE, Arrays.asList("image/jpeg")),
    JS   ("js",  FileTypeDiffability.AS_TEXT,  Arrays.asList("application/javascript")),
    JSON ("json",FileTypeDiffability.AS_TEXT,  Arrays.asList("application/json")),

    /**
     * Markdown text file
     */
    MD   ("md",  FileTypeDiffability.AS_TEXT,  Arrays.asList(""), "Markdown text"),

    /**
     * MIME HTML file
     */
    MHTML("mht", FileTypeDiffability.AS_TEXT,  Arrays.asList(""), "MIME HTML"),
    PDF  ("pdf", FileTypeDiffability.UNABLE,   Arrays.asList("application/pdf")),

    /**
     * Portable Network Graphics
     */
    PNG  ("png", FileTypeDiffability.AS_IMAGE, Arrays.asList("image/png")),

    /**
     * Project Object Model file of Maven
     */
    POM  ("pom", FileTypeDiffability.AS_TEXT,  Arrays.asList("application/xml"), "Maven Project Object Model XML"),

    /**
     * Microsoft PowerPoint file
     */
    PPT  ("ppt", FileTypeDiffability.UNABLE,   Arrays.asList("application/vnd.ms-powerpoint")),
    PPTX ("pptx",FileTypeDiffability.UNABLE,   Arrays.asList("application/vnd.openxmlformats-officedocument.presentationml.presentation")),

    /**
     * Scalable Vector Graphics file
     */
    SVG  ("svg", FileTypeDiffability.AS_TEXT,  Arrays.asList("image/svg+xml")),
    TAR  ("tar", FileTypeDiffability.UNABLE,   Arrays.asList("application/x-tar")),
    TGZ  ("tgz", FileTypeDiffability.UNABLE,   Arrays.asList(
            "application/zlib",
            "application/gzip")),
    /**
     * a Text file with extention ".txt"
     */
    TXT  ("txt", FileTypeDiffability.AS_TEXT,  Arrays.asList("text/plain")),

    /**
     * Micorosft Excel file
     */
    XLS  ("xls", FileTypeDiffability.UNABLE,   Arrays.asList(
            "application/vnd.ms-excel",
            "application/msexcel",
            "application/x-msexcel",
            "application/x-ms-excel",
            "application/x-excel",
            "application/x-dos_mx_excel",
            "application/xls",
            "application/x-xls",
            "application/vnd-ms-office",
            "application/vnd-xls",
            "application/octet-stream")),
    /**
     * Microsoft Excel file with Macro
     */
    XLSM ("xlsm", FileTypeDiffability.UNABLE,   Arrays.asList("application/vnd.ms-excel")),
    XLSX ("xlsx", FileTypeDiffability.UNABLE,   Arrays.asList(
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
    XML  ("xml",  FileTypeDiffability.AS_TEXT,  Arrays.asList("application/xml")),
    ZIP  ("zip",  FileTypeDiffability.UNABLE,   Arrays.asList(
            "application/zip",
            "application/x-zip-compressed")),
    /**
     * Web Open Font Format
     */
    WOFF2 ("woff2", FileTypeDiffability.UNABLE, Arrays.asList("font/woff2")),

    /**
     * a file extension ".UNSUPPORTED"
     */
    UNSUPPORTED ("UNSUPPORTED", FileTypeDiffability.UNABLE, Arrays.asList(""), "Unsupported FileType"),

    /**
     * an empty string "" as file extension
     */
    NULL_OBJECT("",  FileTypeDiffability.UNABLE, Arrays.asList(""), "NULL Object"),

    /**
     * https://github.com/kazurayam/materialstore/issues/423
     */
    NO_COUNTERPART("no_counterpart", FileTypeDiffability.UNABLE, Arrays.asList(""), "Diff when no counterpart found")
    ;

    private final String extension_;
    private final List<String> mimeTypes_;
    private final FileTypeDiffability diffability_;
    private final String description_;

    FileType(String extension, FileTypeDiffability diffability, List<String> mimeTypes,
             String description) {
        this.extension_ = extension;
        this.diffability_ = diffability;
        this.mimeTypes_  = mimeTypes;
        this.description_ = description;
    }

    FileType(String extension, FileTypeDiffability diffability, List<String> mimeTypes) {
        this(extension, diffability, mimeTypes, "");
    }

    @Override
    public String getExtension() {
        return this.extension_;
    }

    @Override
    public FileTypeDiffability getDiffability() {
        return this.diffability_;
    }

    @Override
    public List<String> getMimeTypes() {
        return this.mimeTypes_;
    }

    @Override
    public String toString() {
        return toJson(); // wondered if "this.getExtension()" might be better suitable
    }

    // toJson() method is implemented in the IFileType interface

}