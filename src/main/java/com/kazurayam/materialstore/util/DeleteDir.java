package com.kazurayam.materialstore.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Objects;

public class DeleteDir {

    public static void deleteDirectoryRecursively(Path dir)
            throws IOException {
        Objects.requireNonNull(dir);
        if (!Files.exists(dir)) {
            throw new IOException(dir + " does not exist");
        }
        try {
            Files.walk(dir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            if (Files.exists(p)) {
                                Files.delete(p);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
