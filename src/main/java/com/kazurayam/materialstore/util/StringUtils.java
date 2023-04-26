package com.kazurayam.materialstore.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class StringUtils {

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

    public static String repeat(String str, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    public static String indentLines(String text) {
        return indentLines(text, 4);
    }

    public static String indentLines(String text, int indentWidth) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        StringReader sr = new StringReader(text);
        BufferedReader br = new BufferedReader(sr);
        String line;
        try {
            while ((line = br.readLine()) != null) {
                pw.println(String.join("", Collections.nCopies(indentWidth, " ")) + line);
            }
            br.close();
            sr.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        pw.flush();
        pw.close();
        return sw.toString();
    }

    /**
     * accept a string, split it into multiple lines by NEW LINES char,
     * return a List of String.
     *
     * @param content a String that contains zero or more NEW LINE character(s)
     * @return a List of String
     */
    public static List<String> toList(String content) {
        StringReader sr = new StringReader(content);
        BufferedReader br = new BufferedReader(sr);
        List<String> lines = new ArrayList<>();
        String line;
        try {
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            // will never fall down into here
            e.printStackTrace();
        }
        return lines;
    }

    /*
     * https://www.baeldung.com/java-random-string
     *
     * @param length of the resulting String
     * @returns a String which contains random
     */
    public static String generateRandomAlphaNumericString(int length) {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = length;
        Random random = new Random();
        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
