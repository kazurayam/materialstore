package com.kazurayam.materialstore.report;

import com.kazurayam.materialstore.MaterialstoreException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class AbstractReporter {

    protected boolean DEBUG = false;

    void setDebug(boolean debug) {
        this.DEBUG = debug;
    }

    boolean isDebug() {
        return this.DEBUG;
    }

    String getTitle(Path file) {
        String fileName = file.getFileName().toString();
        return fileName.substring(0, fileName.indexOf(".html"));
    }


    protected void writeModel(String modelJson, Path dir) throws MaterialstoreException {
        try {
            Path file = dir.resolve("model.json");
            Files.write(file, modelJson.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new MaterialstoreException(e);
        }
    }
}
