package com.kazurayam.materialstore.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JsonUtil {

    private JsonUtil() {}

    public static String escapeAsJsonString(String value) {
        char[] chars = value.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (char aChar : chars) {
            switch (aChar) {
                // Backspace
                case '\b':
                    sb.append("\\b");
                    break;
                // Form feed
                case '\f':
                    sb.append("\\f");
                    break;
                // Newline
                case '\n':
                    sb.append("\\n");
                    break;
                // Carriage return
                case '\r':
                    sb.append("\\r");
                    break;
                // Tab
                case '\t':
                    sb.append("\\t");
                    break;
                // Double quote
                case '\"':
                    sb.append("\\\"");
                    break;
                // Backslash
                case '\\':
                    sb.append("\\\\");
                    break;
                // others
                default:
                    sb.append(aChar);
                    break;
            }
        }
        return sb.toString();
    }

    public static String prettyPrint(String sourceJson) {
        return prettyPrint(sourceJson, Map.class);
    }

    public static <T> String prettyPrint(String sourceJson, Class<T> clazz) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        // parse JSON text to a Map
        Object obj = gson.fromJson(sourceJson, clazz);
        // serialize the object back to a JSON text in pretty-print format
        return gson.toJson(obj);
    }

    /**
     * Given with an Exception:
     * Caused by: com.google.gson.stream.MalformedJsonException: Expected ':' at line 1 column 8 path $..8
     *
     * log the following:
     *
     * line ---------1---------2--- ... --9---------0---------1--- ...
     *    1 { "foo", "bar" }
     *             ^
     */
    public static final Pattern LINE_COLUMN_PATTERN = Pattern.compile("at line (\\d+) column (\\d+)");

    public static void logJsonSyntaxException(String json, JsonSyntaxException e, Logger logger) throws IOException {
        String msg = e.getMessage();
        Matcher matcher = LINE_COLUMN_PATTERN.matcher(msg);
        if (matcher.find()) {
            int atLine = Integer.parseInt(matcher.group(1));
            int atColumn = Integer.parseInt(matcher.group(2));
            logger.warn("JsonSyntaxException at line " + atLine + " column " + atColumn);
            BufferedReader br = new BufferedReader(new StringReader(json));
            String line;
            int lineCount = 1;
            while ((line = br.readLine()) != null) {
                logger.info(String.format("%4d %s", lineCount, line));
                if (atLine == lineCount) {
                    String indent = String.join("", Collections.nCopies(atColumn - 1, " "));
                    logger.info(String.format("^^^^ %s^", indent));
                }
                lineCount++;
            }
        }
    }
}
