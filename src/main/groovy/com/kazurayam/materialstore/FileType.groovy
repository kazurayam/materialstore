package com.kazurayam.materialstore

final enum FileType {

    BMP  ('bmp',    ['image/bmp']),
    CSS ('css', ['text/css']),
    CSV  ('csv',    ['text/csv', 'text/plain']),
    DOC  ('doc',    ['application/msword']),
    DOCX ('docx',   ['application/vnd.openxmlformats-officedocument.wordprocessingml.document']),
    GIF  ('gif',    ['image/gif']),
    HTML ('html',   ['text/html']),
    JAR  ('jar',    ['application/java-archive']),
    JPG  ('jpg',    ['image/jpeg']),
    JPEG ('jpeg',   ['image/jpeg']),
    JS ('js', ['application/javascript']),
    JSON ('json',   ['application/json']),
    MD   ('md',     [''], "Markdown text"),
    MHTML('mht',    [''], "MIME HTML"),
    PDF  ('pdf',    ['application/pdf']),
    PNG  ('png' ,   ['image/png']),
    POM  ('pom',    ['application/xml']),
    PPT  ('ppt',    ['application/vnd.ms-powerpoint']),
    PPTX ('pptx',   ['application/vnd.openxmlformats-officedocument.presentationml.presentation']),
    SVG  ('svg',    ['image/svg+xml']),
    TAR  ('tar',    ['application/x-tar']),
    TGZ  ('tgz',    [
            'application/zlib',
            'application/gzip']),
    TXT  ('txt',    ['text/plain']),
    XLS  ('xls',    [
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
    XLSM ('xlsm',   ['application/vnd.ms-excel']),
    XLSX ('xlsx',   [
            'application/vnd.ms-excel',
            'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet']),
    XML  ('xml',    ['application/xml']),
    ZIP  ('zip',    [
            'application/zip',
            'application/x-zip-compressed']),
    WOFF2 ('woff2', ['font/woff2']),

    UNSUPPORTED ('UNSUPPORTED',    [''], "Unsupported FileType"),
    NULL ('',       [''], "NULL Object") ;

    private final String extension_
    private final List<String> mimeTypes_
    private final String description_

    FileType(String extension, List<String> mimeTypes, String description = "") {
        this.extension_ = extension
        this.mimeTypes_  = mimeTypes
        this.description_ = description
    }

    String getExtension() {
        return this.extension_
    }

    List<String> getMimeTypes() {
        return this.mimeTypes_
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
        return NULL
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