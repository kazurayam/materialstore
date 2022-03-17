package com.kazurayam.materialstore.report;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractReporterTest {

    List<String> trimLines(List<String> source) {
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
}
