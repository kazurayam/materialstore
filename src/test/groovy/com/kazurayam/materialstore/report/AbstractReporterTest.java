package com.kazurayam.materialstore.report;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractReporterTest {

    public List<String> trimLines(List<String> source) {
        List<String> trimmed = new ArrayList<>();
        for (String line : source) {
            String s = line.trim()
                    .replaceAll("&quot;", "\"")
                    .replaceAll("&#39;", "'");
            if (s.length() > 0) {
                trimmed.add(s);
            }
        }
        return trimmed;
    }

    String readString(Path file) throws IOException {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        List<String> lines = Files.readAllLines(file);
        for (String line : lines) {
            pw.println(line);
        }
        pw.flush();
        pw.close();
        return sw.toString();
    }
}
