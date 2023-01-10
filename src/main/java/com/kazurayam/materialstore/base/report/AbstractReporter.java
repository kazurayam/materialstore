package com.kazurayam.materialstore.base.report;

import com.kazurayam.materialstore.core.MaterialstoreException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class AbstractReporter implements HTMLPrettyPrintingCapable {

    protected boolean verboseLogging = false;
    protected boolean prettyPrinting = false;

    void enableVerboseLogging(boolean verboseLogging) {
        this.verboseLogging = verboseLogging;
    }

    boolean isVerboseLoggingEnabled() {
        return this.verboseLogging;
    }

    @Override
    public void enablePrettyPrinting(boolean prettyPrinting) {
        this.prettyPrinting = prettyPrinting;
    }

    @Override
    public boolean isPrettyPrintingEnabled() { return this.prettyPrinting; }


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
