package com.kazurayam.taod

enum FileType {

    TXT  ('txt',    ['text/plain']),
    CSV  ('csv',    [
            'text/csv',
            'text/plain']),
    BMP  ('bmp',    ['image/bmp']),
    GIF  ('gif',    ['image/gif']),
    HTML ('html',   ['text/html']),
    JPG  ('jpg',    ['image/jpeg']),
    JPEG ('jpeg',   ['image/jpeg']),
    PNG  ('png' ,   ['image/png']),
    SVG  ('svg',   ['image/svg+xml']),
    JSON ('json',   ['application/json']),
    PDF  ('pdf',    ['application/pdf']),
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
    POM  ('pom',    ['application/xml']),
    JAR  ('jar',    ['application/java-archive']),
    TAR  ('tar',    ['application/x-tar']),
    ZIP  ('zip',    [
            'application/zip',
            'application/x-zip-compressed']),
    TGZ  ('tgz',    [
            'application/zlib',
            'application/gzip']),
    DOC  ('doc',    ['application/msword']),
    PPT  ('ppt',    ['application/vnd.ms-powerpoint']),
    DOCX ('docx',   ['application/vnd.openxmlformats-officedocument.wordprocessingml.document']),
    PPTX ('pptx',   ['application/vnd.openxmlformats-officedocument.presentationml.presentation']),

    UNSUPPORTED ('UNSUPPORTED',    ['']),
    NULL ('',       ['']) ;

    private final String extension_
    private final List<String> mimeTypes_

    FileType(String extension, List<String> mimeTypes) {
        this.extension_ = extension
        this.mimeTypes_  = mimeTypes
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