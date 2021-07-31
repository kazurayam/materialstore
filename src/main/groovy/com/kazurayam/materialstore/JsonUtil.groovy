package com.kazurayam.materialstore

class JsonUtil {

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
}
