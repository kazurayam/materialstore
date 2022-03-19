package com.kazurayam.materialstore.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import org.slf4j.Logger

import java.util.regex.Matcher
import java.util.regex.Pattern

final class JsonUtil {

    private JsonUtil() {}

    static final String escapeAsJsonString(String value) {
        char[] chars = value.toCharArray()
        StringBuilder sb = new StringBuilder()
        for (int i = 0; i < chars.length; i++) {
            switch (chars[i]) {
            // Backspace
                case '\b': sb.append("\\b")
                    break
                    // Form feed
                case '\f': sb.append("\\f")
                    break
                    // Newline
                case '\n': sb.append("\\n")
                    break
                    // Carriage return
                case '\r': sb.append("\\r")
                    break
                    // Tab
                case '\t': sb.append("\\t")
                    break
                    // Double quote
                case '\"': sb.append('\\"')
                    break
                    // Backslash
                case '\\': sb.append('\\\\')
                    break
                    // others
                default: sb.append(chars[i])
                    break
            }
        }
        return sb.toString()
    }

    static String prettyPrint(String sourceJson) {
        return prettyPrint(sourceJson, Map.class)
    }

    static String prettyPrint(String sourceJson, Class clazz) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create()
        // parse JSON text to an Map
        Object obj = gson.fromJson(sourceJson, clazz)
        // serialize the object back to a JSON text in pretty-print format
        String multiLineJson = gson.toJson(obj)
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
     *
     * @param json
     * @param e
     * @param logger
     */
    static Pattern LINE_COLUMN_PATTERN = Pattern.compile("at line (\\d+) column (\\d+)")
    static void logJsonSyntaxException(String json, JsonSyntaxException e, Logger logger) {
        String msg = e.getMessage()
        Matcher matcher = LINE_COLUMN_PATTERN.matcher(msg)
        if (matcher.find()) {
            int atLine = Integer.parseInt(matcher.group(1))
            int atColumn = Integer.parseInt(matcher.group(2))
            logger.warn("JsonSyntaxException at line " + atLine + " column " + atColumn)
            StringReader sr = new StringReader(json)
            String line
            int lineCount = 1
            while ((line = sr.readLine()) != null) {
                logger.info(String.format("%4d %s", lineCount, line))
                if (atLine == lineCount) {
                    String indent = String.join("", Collections.nCopies(atColumn - 1, " "))
                    logger.info(String.format("^^^^ %s^", indent))
                }
                lineCount++
            }
        }
    }
}
