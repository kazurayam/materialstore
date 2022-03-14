package com.kazurayam.materialstore.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder

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
}
