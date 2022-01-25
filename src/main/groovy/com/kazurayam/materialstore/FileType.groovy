package com.kazurayam.materialstore

import java.util.stream.Collectors

final enum FileType {

    BMP  ('bmp', Diffability.AS_IMAGE, ['image/bmp']),
    CSS  ('css', Diffability.AS_TEXT,  ['text/css']),
    CSV  ('csv', Diffability.AS_TEXT,  ['text/csv', 'text/plain']),
    DOC  ('doc', Diffability.UNABLE,   ['application/msword']),
    DOCX ('docx',Diffability.UNABLE,   ['application/vnd.openxmlformats-officedocument.wordprocessingml.document']),
    GIF  ('gif', Diffability.AS_IMAGE, ['image/gif']),
    HTML ('html',Diffability.AS_TEXT,  ['text/html']),
    JAR  ('jar', Diffability.UNABLE,   ['application/java-archive']),
    JPG  ('jpg', Diffability.AS_IMAGE, ['image/jpeg']),
    JPEG ('jpeg',Diffability.AS_IMAGE, ['image/jpeg']),
    JS   ('js',  Diffability.AS_TEXT,  ['application/javascript']),
    JSON ('json',Diffability.AS_TEXT,  ['application/json']),
    MD   ('md',  Diffability.AS_TEXT,  [''], "Markdown text"),
    MHTML('mht', Diffability.AS_TEXT,  [''], "MIME HTML"),
    PDF  ('pdf', Diffability.UNABLE,   ['application/pdf']),
    PNG  ('png', Diffability.AS_IMAGE, ['image/png']),
    POM  ('pom', Diffability.AS_TEXT,  ['application/xml'], "Maven Project Object Model XML"),
    PPT  ('ppt', Diffability.UNABLE,   ['application/vnd.ms-powerpoint']),
    PPTX ('pptx',Diffability.UNABLE,   ['application/vnd.openxmlformats-officedocument.presentationml.presentation']),
    SVG  ('svg', Diffability.AS_TEXT,  ['image/svg+xml']),
    TAR  ('tar', Diffability.UNABLE,   ['application/x-tar']),
    TGZ  ('tgz', Diffability.UNABLE,   [
            'application/zlib',
            'application/gzip']),
    TXT  ('txt', Diffability.AS_TEXT,  ['text/plain']),
    XLS  ('xls', Diffability.UNABLE,   [
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
    XLSM ('xlsm', Diffability.UNABLE,   ['application/vnd.ms-excel']),
    XLSX ('xlsx', Diffability.UNABLE,   [
            'application/vnd.ms-excel',
            'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet']),
    XML  ('xml',  Diffability.AS_TEXT,  ['application/xml']),
    ZIP  ('zip',  Diffability.UNABLE,   [
            'application/zip',
            'application/x-zip-compressed']),
    WOFF2 ('woff2', Diffability.UNABLE, ['font/woff2']),

    UNSUPPORTED ('UNSUPPORTED', Diffability.UNABLE, [''], "Unsupported FileType"),
    NULL_OBJECT('',  Diffability.UNABLE, [''], "NULL Object") ;

    private final String extension_
    private final List<String> mimeTypes_
    private final Diffability diffability_
    private final String description_

    FileType(String extension, Diffability diffability, List<String> mimeTypes,
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

    Diffability getDiffability() {
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
        sb.append(',"diffability":"')
        sb.append(this.getDiffability())
        sb.append('"')
        sb.append(']')
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
                    ft.getDiffability() == Diffability.AS_TEXT
                })
                .collect(Collectors.toList())
    }

    static List<FileType> getFileTypesDiffableAsImage() {
        List<FileType> values = FileType.values() as List
        return values.stream()
                .filter({ ft ->
                    ft.getDiffability() == Diffability.AS_IMAGE
                })
                .collect(Collectors.toList())
    }

    static List<FileType> getFileTypesUnableToDiff() {
        List<FileType> values = FileType.values() as List
        return values.stream()
                .filter({ ft ->
                    ft.getDiffability() == Diffability.UNABLE
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