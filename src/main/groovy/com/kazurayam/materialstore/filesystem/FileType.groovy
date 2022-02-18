package com.kazurayam.materialstore.filesystem

import java.util.stream.Collectors

final enum FileType {

    BMP  ('bmp', FileTypeDiffability.AS_IMAGE, ['image/bmp']),
    CSS  ('css', FileTypeDiffability.AS_TEXT,  ['text/css']),
    CSV  ('csv', FileTypeDiffability.AS_TEXT,  ['text/csv', 'text/plain']),
    DOC  ('doc', FileTypeDiffability.UNABLE,   ['application/msword']),
    DOCX ('docx',FileTypeDiffability.UNABLE,   ['application/vnd.openxmlformats-officedocument.wordprocessingml.document']),
    GIF  ('gif', FileTypeDiffability.AS_IMAGE, ['image/gif']),
    HTML ('html',FileTypeDiffability.AS_TEXT,  ['text/html']),
    JAR  ('jar', FileTypeDiffability.UNABLE,   ['application/java-archive']),
    JPG  ('jpg', FileTypeDiffability.AS_IMAGE, ['image/jpeg']),
    JPEG ('jpeg',FileTypeDiffability.AS_IMAGE, ['image/jpeg']),
    JS   ('js',  FileTypeDiffability.AS_TEXT,  ['application/javascript']),
    JSON ('json',FileTypeDiffability.AS_TEXT,  ['application/json']),
    MD   ('md',  FileTypeDiffability.AS_TEXT,  [''], "Markdown text"),
    MHTML('mht', FileTypeDiffability.AS_TEXT,  [''], "MIME HTML"),
    PDF  ('pdf', FileTypeDiffability.UNABLE,   ['application/pdf']),
    PNG  ('png', FileTypeDiffability.AS_IMAGE, ['image/png']),
    POM  ('pom', FileTypeDiffability.AS_TEXT,  ['application/xml'], "Maven Project Object Model XML"),
    PPT  ('ppt', FileTypeDiffability.UNABLE,   ['application/vnd.ms-powerpoint']),
    PPTX ('pptx',FileTypeDiffability.UNABLE,   ['application/vnd.openxmlformats-officedocument.presentationml.presentation']),
    SVG  ('svg', FileTypeDiffability.AS_TEXT,  ['image/svg+xml']),
    TAR  ('tar', FileTypeDiffability.UNABLE,   ['application/x-tar']),
    TGZ  ('tgz', FileTypeDiffability.UNABLE,   [
            'application/zlib',
            'application/gzip']),
    TXT  ('txt', FileTypeDiffability.AS_TEXT,  ['text/plain']),
    XLS  ('xls', FileTypeDiffability.UNABLE,   [
            'application/vnd.ms-excel',
            'application/msexcel',
            'application/x-msexcel',
            'application/x-ms-excel',
            'application/x-excel',
            'application/x-dos_mx_excel',
            'application/xls',
            'application/x-xls',
            'application/vnd-ms-office',
            'application/vnd-xls',
            'application/octet-stream']),
    XLSM ('xlsm', FileTypeDiffability.UNABLE,   ['application/vnd.ms-excel']),
    XLSX ('xlsx', FileTypeDiffability.UNABLE,   [
            'application/vnd.ms-excel',
            'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet']),
    XML  ('xml',  FileTypeDiffability.AS_TEXT,  ['application/xml']),
    ZIP  ('zip',  FileTypeDiffability.UNABLE,   [
            'application/zip',
            'application/x-zip-compressed']),
    WOFF2 ('woff2', FileTypeDiffability.UNABLE, ['font/woff2']),

    UNSUPPORTED ('UNSUPPORTED', FileTypeDiffability.UNABLE, [''], "Unsupported FileType"),
    NULL_OBJECT('',  FileTypeDiffability.UNABLE, [''], "NULL Object") ;

    private final String extension_
    private final List<String> mimeTypes_
    private final FileTypeDiffability diffability_
    private final String description_

    FileType(String extension, FileTypeDiffability diffability, List<String> mimeTypes,
             String description = "") {
        this.extension_ = extension
        this.diffability_ = diffability
        this.mimeTypes_  = mimeTypes
        this.description_ = description
    }

    String getExtension() {
        return this.extension_
    }

    List<String> getMimeTypes() {
        return this.mimeTypes_
    }

    FileTypeDiffability getDiffability() {
        return this.diffability_
    }

    @Override
    String toString() {
        return toJsonText() //this.getExtension()
    }

    String toJsonText() {
        StringBuilder sb = new StringBuilder()
        sb.append('{"FileType":{')
        sb.append('"extension":"' + this.getExtension() + '","mimeTypes":[')
        def count = 0
        for (String mimetype: this.getMimeTypes()) {
            if (count > 0) {
                sb.append(',')
            }
            count += 1
            sb.append('"' + mimetype + '"')
        }
        sb.append(']')
        sb.append(',"diffability":"')
        sb.append(this.getDiffability())
        sb.append('"')
        sb.append('}}')
        return sb.toString()
    }

    static FileType getByExtension(String ext) {
        for (FileType v : values()) {
            if (v.getExtension().toLowerCase() == ext.toLowerCase()) {
                return v
            }
        }
        return FileType.UNSUPPORTED
    }

    static FileType ofMimeType(String mimeType) {
        for (FileType v : values()) {
            List<String> mimeTypes = v.getMimeTypes()
            if (mimeTypes.contains(mimeType)) {
                return v
            }
        }
        return NULL_OBJECT
    }

    static List<FileType> getFileTypesDiffableAsText() {
        List<FileType> values = FileType.values() as List
        return values.stream()
                .filter({ ft ->
                    ft.getDiffability() == FileTypeDiffability.AS_TEXT
                })
                .collect(Collectors.toList())
    }

    static List<FileType> getFileTypesDiffableAsImage() {
        List<FileType> values = FileType.values() as List
        return values.stream()
                .filter({ ft ->
                    ft.getDiffability() == FileTypeDiffability.AS_IMAGE
                })
                .collect(Collectors.toList())
    }

    static List<FileType> getFileTypesUnableToDiff() {
        List<FileType> values = FileType.values() as List
        return values.stream()
                .filter({ ft ->
                    ft.getDiffability() == FileTypeDiffability.UNABLE
                })
                .collect(Collectors.toList())
    }

    /**
     * returns a string which contains all of MIME types listed uniquely in comma-separated format.
     * Example:
     * <pre>application/json,application/msexcel,application/pdf,application/vnd-ms-office,application/vnd-xls,application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/x-dos_mx_excel,application/x-excel,application/x-ms-excel,application/x-msexcel,application/x-xls,application/xls,application/xml,image/bmp,image/gif,image/jpeg,image/png,text/plain</pre>
     * @return
     */
    static List<String> getAllMimeTypes() {
        Set<String> mimetypeSet = new HashSet<String>()
        for (FileType v : values()) {
            mimetypeSet.addAll(v.getMimeTypes())
        }
        List<String> mimetypeList = new ArrayList(mimetypeSet)
        Collections.sort(mimetypeList)
        return mimetypeList
    }

    static String getAllMimeTypesAsString() {
        List<String> mimetypeList = getAllMimeTypes()
        StringBuilder sb = new StringBuilder()
        int count = 0
        for (String mimetype : mimetypeList) {
            if (mimetype.length() > 0) {
                if (count > 0) {
                    sb.append(",")
                }
                count += 1
                sb.append(mimetype)
            }
        }
        return sb.toString()
    }
}