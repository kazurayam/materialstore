package com.kazurayam.materialstore.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

public class StringUtils {

    private StringUtils() {}

    public static String join(List<String> lines) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        for (String line : lines) {
            pw.println(line);
        }
        pw.flush();
        pw.close();
        return sw.toString();
    }
}
