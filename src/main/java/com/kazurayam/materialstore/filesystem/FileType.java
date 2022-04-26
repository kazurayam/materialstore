package com.kazurayam.materialstore.filesystem;

import com.kazurayam.materialstore.util.JsonUtil;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum FileType implements Jsonifiable, TemplateReady {

    BMP  ("bmp", FileTypeDiffability.AS_IMAGE, Arrays.asList("image/bmp")),
    CSS  ("css", FileTypeDiffability.AS_TEXT,  Arrays.asList("text/css")),
    CSV  ("csv", FileTypeDiffability.AS_TEXT,  Arrays.asList("text/csv", "text/plain")),
    DOC  ("doc", FileTypeDiffability.UNABLE,   Arrays.asList("application/msword")),
    DOCX ("docx",FileTypeDiffability.UNABLE,   Arrays.asList("application/vnd.openxmlformats-officedocument.wordprocessingml.document")),
    GIF  ("gif", FileTypeDiffability.AS_IMAGE, Arrays.asList("image/gif")),
    HTML ("html",FileTypeDiffability.AS_TEXT,  Arrays.asList("text/html")),
    JAR  ("jar", FileTypeDiffability.UNABLE,   Arrays.asList("application/java-archive")),
    JPG  ("jpg", FileTypeDiffability.AS_IMAGE, Arrays.asList("image/jpeg")),
    JPEG ("jpeg",FileTypeDiffability.AS_IMAGE, Arrays.asList("image/jpeg")),
    JS   ("js",  FileTypeDiffability.AS_TEXT,  Arrays.asList("application/javascript")),
    JSON ("json",FileTypeDiffability.AS_TEXT,  Arrays.asList("application/json")),
    MD   ("md",  FileTypeDiffability.AS_TEXT,  Arrays.asList(""), "Markdown text"),
    MHTML("mht", FileTypeDiffability.AS_TEXT,  Arrays.asList(""), "MIME HTML"),
    PDF  ("pdf", FileTypeDiffability.UNABLE,   Arrays.asList("application/pdf")),
    PNG  ("png", FileTypeDiffability.AS_IMAGE, Arrays.asList("image/png")),
    POM  ("pom", FileTypeDiffability.AS_TEXT,  Arrays.asList("application/xml"), "Maven Project Object Model XML"),
    PPT  ("ppt", FileTypeDiffability.UNABLE,   Arrays.asList("application/vnd.ms-powerpoint")),
    PPTX ("pptx",FileTypeDiffability.UNABLE,   Arrays.asList("application/vnd.openxmlformats-officedocument.presentationml.presentation")),
    PUML ("puml",FileTypeDiffability.AS_TEXT,  Arrays.asList("text/plain")),
    SVG  ("svg", FileTypeDiffability.AS_TEXT,  Arrays.asList("image/svg+xml")),
    TAR  ("tar", FileTypeDiffability.UNABLE,   Arrays.asList("application/x-tar")),
    TGZ  ("tgz", FileTypeDiffability.UNABLE,   Arrays.asList(
            "application/zlib",
            "application/gzip")),
    TXT  ("txt", FileTypeDiffability.AS_TEXT,  Arrays.asList("text/plain")),
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
    XLSM ("xlsm", FileTypeDiffability.UNABLE,   Arrays.asList("application/vnd.ms-excel")),
    XLSX ("xlsx", FileTypeDiffability.UNABLE,   Arrays.asList(
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
    XML  ("xml",  FileTypeDiffability.AS_TEXT,  Arrays.asList("application/xml")),
    ZIP  ("zip",  FileTypeDiffability.UNABLE,   Arrays.asList(
            "application/zip",
            "application/x-zip-compressed")),
    WOFF2 ("woff2", FileTypeDiffability.UNABLE, Arrays.asList("font/woff2")),

    UNSUPPORTED ("UNSUPPORTED", FileTypeDiffability.UNABLE, Arrays.asList(""), "Unsupported FileType"),
    NULL_OBJECT("",  FileTypeDiffability.UNABLE, Arrays.asList(""), "NULL Object")
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

    public String getExtension() {
        return this.extension_;
    }

    public List<String> getMimeTypes() {
        return this.mimeTypes_;
    }

    public FileTypeDiffability getDiffability() {
        return this.diffability_;
    }

    @Override
    public String toString() {
        return toJson(); //this.getExtension()
    }

    @Override
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"extension\":\"");
        sb.append(this.getExtension());
        sb.append("\"");
        sb.append(",");
        sb.append("\"mimeTypes\":[");
        int count = 0;
        for (String mimetype: this.getMimeTypes()) {
            if (count > 0) {
                sb.append(",");
            }
            count += 1;
            sb.append("\"");
            sb.append(mimetype);
            sb.append("\"");
        }
        sb.append("]");
        sb.append(",\"diffability\":\"");
        sb.append(this.getDiffability());
        sb.append("\"");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public String toJson(boolean prettyPrint) {
        if (prettyPrint) {
            return JsonUtil.prettyPrint(toJson());
        } else {
            return toJson();
        }
    }


    //-----------------------------------------------------------------
    public static FileType getByExtension(String ext) {
        for (FileType v : values()) {
            if (v.getExtension().equalsIgnoreCase(ext)) {
                return v;
            }
        }
        return UNSUPPORTED;
    }

    public static FileType ofMimeType(String mimeType) {
        for (FileType v : values()) {
            List<String> mimeTypes = v.getMimeTypes();
            if (mimeTypes.contains(mimeType)) {
                return v;
            }
        }
        return NULL_OBJECT;
    }

    public static List<FileType> getFileTypesDiffableAsText() {
        List<FileType> values = Arrays.asList(values());
        return values.stream()
                .filter(ft -> ft.getDiffability() == FileTypeDiffability.AS_TEXT)
                .collect(Collectors.toList());
    }

    public static List<FileType> getFileTypesDiffableAsImage() {
        List<FileType> values = Arrays.asList(values());
        return values.stream()
                .filter(ft -> ft.getDiffability() == FileTypeDiffability.AS_IMAGE)
                .collect(Collectors.toList());
    }

    public static List<FileType> getFileTypesUnableToDiff() {
        List<FileType> values = Arrays.asList(values());
        return values.stream()
                .filter(ft -> ft.getDiffability() == FileTypeDiffability.UNABLE)
                .collect(Collectors.toList());
    }
}