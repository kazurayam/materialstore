package com.kazurayam.materialstore.core.filesystem.metadata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SemanticVersionPattern {

    private static final Logger logger = LoggerFactory.getLogger(SemanticVersionPattern.class);

    static final String REGEX_HEADER = "(\\S+)";
    static final String REGEX_VERSION = "(\\d+\\.\\d+\\.\\d+(-[a-zA-Z][0-9a-zA-Z]*)?)";
    static final String REGEX_TRAILER = "(\\S*)";
    static final Pattern VERSIONED_PATH_PARSER = Pattern.compile(REGEX_HEADER + REGEX_VERSION + REGEX_TRAILER);

    private final Pattern pattern;

    public SemanticVersionPattern(String baseStr) {
        Objects.requireNonNull(baseStr);
        this.pattern = translateToBaseStrToPattern(baseStr);
    }

    /**
     * Compare the leftPath and the rightPath are similar.
     * If the 2 strings contain a Semantic Version, then the version is
     * smartly disregarded.
     * <p>
     * E.g,
     * (1) "/some/path/x" and "/some/path/x" will return true
     * (2) "/some/path/x" and "/some/path/Y" will return false
     * (3) "/some/path-1.2.0/x" and "/some/path-1.2.3-alpha/x" will return true
     *
     * @param leftStr TODO
     * @return if leftStr is identical to the baseStr, return true; otherwise false
     */
    public Matcher matcher(String leftStr) {
        Objects.requireNonNull(leftStr);
        return pattern.matcher(leftStr.trim());
    }

    public Pattern pattern() {
        return pattern;
    }

    /**
     * returns a java.util.regex.Matcher for the string.
     * The matcher.matches() will return true if the string contains
     * a semantic version like "0.1.2-alpha".
     *
     * @param string TODO
     * @return TODO
     */
    public static Matcher straightMatcher(String string) {
        Objects.requireNonNull(string);
        return VERSIONED_PATH_PARSER.matcher(string);
    }

    public static Pattern translateToBaseStrToPattern(String baseStr) {
        Objects.requireNonNull(baseStr);
        Matcher m = VERSIONED_PATH_PARSER.matcher(baseStr);
        if (m.matches()) {
            // the path contains a semantic version (e.g, '1.5.3-rc')
            if (m.groupCount() < 4) {
                String h = m.group(1);
                StringBuilder sb = new StringBuilder();
                sb.append("(" + escapeAsRegex(h) + ")");
                sb.append(REGEX_VERSION);
                return Pattern.compile(sb.toString());
            } else {
                String h = m.group(1);
                String t = m.group(4);
                StringBuilder sb = new StringBuilder();
                sb.append("(" + escapeAsRegex(h) + ")");
                sb.append(REGEX_VERSION);
                sb.append("(" + escapeAsRegex(t) + ")");
                return Pattern.compile(sb.toString());
            }

        } else {
            // the path has no version
            return Pattern.compile("(" + escapeAsRegex(baseStr) + ")");
        }

    }

    public static String escapeAsRegex(String path) {
        Objects.requireNonNull(path);
        return path
                .replace("/", "\\/")
                .replace(".", "\\.")
                .replace("'", "\\'")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("-", "\\-")
                .replace("[", "\\[")
                .replace("]", "\\]");
    }

}
