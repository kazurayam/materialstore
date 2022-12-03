package com.kazurayam.materialstore.base.materialize;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class URLDownloader {
    public static long download(URL url, Path path) throws IOException {
        InputStream inputStream = url.openStream();
        return Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
    }
}
