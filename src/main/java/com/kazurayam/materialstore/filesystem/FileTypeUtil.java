package com.kazurayam.materialstore.filesystem;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class FileTypeUtil {

    private FileTypeUtil() {}

    //-----------------------------------------------------------------
    public static FileType getByExtension(String ext) {
        for (FileType v : FileType.values()) {
            if (v.getExtension().equalsIgnoreCase(ext)) {
                return v;
            }
        }
        return FileType.UNSUPPORTED;
    }

    public static FileType ofMimeType(String mimeType) {
        for (FileType v : FileType.values()) {
            List<String> mimeTypes = v.getMimeTypes();
            if (mimeTypes.contains(mimeType)) {
                return v;
            }
        }
        return FileType.NULL_OBJECT;
    }

    public static List<FileType> getFileTypesDiffableAsText() {
        List<FileType> values = Arrays.asList(FileType.values());
        return values.stream()
                .filter(ft -> ft.getDiffability() == FileTypeDiffability.AS_TEXT)
                .collect(Collectors.toList());
    }

    public static List<FileType> getFileTypesDiffableAsImage() {
        List<FileType> values = Arrays.asList(FileType.values());
        return values.stream()
                .filter(ft -> ft.getDiffability() == FileTypeDiffability.AS_IMAGE)
                .collect(Collectors.toList());
    }

    public static List<FileType> getFileTypesUnableToDiff() {
        List<FileType> values = Arrays.asList(FileType.values());
        return values.stream()
                .filter(ft -> ft.getDiffability() == FileTypeDiffability.UNABLE)
                .collect(Collectors.toList());
    }
}
