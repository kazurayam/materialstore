package com.kazurayam.materialstore

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.regex.Matcher
import java.util.regex.Pattern

class SemanticVersionAwareStringMatcher {

    private static final Logger logger = LoggerFactory.getLogger(SemanticVersionAwareStringMatcher.class)

    private static final String REGEX_HEADER = '(\\S+)'
    static final String REGEX_VERSION = '(\\d+\\.\\d+\\.\\d+(\\-[a-zA-Z][0-9a-zA-Z]*)?)'
    private static final String REGEX_TRAILER = '(\\S*)'
    static final Pattern VERSIONED_PATH_PARSER = Pattern.compile(REGEX_HEADER + REGEX_VERSION + REGEX_TRAILER)

    private final String right;
    private final Pattern pattern;

    public SemanticVersionAwareStringMatcher(String right) {
        Objects.requireNonNull(right);
        this.right = right;
        this.pattern = translatePathToRegex(right)
    }

    /**
     * Compare the leftPath and the rightPath are similar.
     * If the 2 strings contain a Semantic Version, then the version is
     * smartly disregarded.
     *
     * E.g,
     * (1) "/some/path/x" and "/some/path/x" will be regarded similar, returning true
     * (2) "/some/path/x" and "/some/path/Y" will be regarded NOT similar, returning false
     * (3) "/some/path-1.2.0/x" and "/some/path-1.2.3-alpha/x" will be
     * regarded similar, returning true
     *
     * @param left
     * @param right
     * @return if left and right are identical, return true; otherwise false
     */
    public Matcher matcher(String left) {
        Objects.requireNonNull(left)
        Matcher m = pattern.matcher(left)
        return m
    }

    static final Pattern translatePathToRegex(String path) {
        Objects.requireNonNull(path)
        Matcher m = VERSIONED_PATH_PARSER.matcher(path)
        if (m.matches()) {
            // the path contains a semantic version (e.g, '1.5.3-rc')
            String h = m.group(1)
            String t = m.group(4)
            StringBuilder sb = new StringBuilder()
            sb.append(escapeAsRegex(h))
            sb.append(REGEX_VERSION)
            sb.append(escapeAsRegex(t))
            return Pattern.compile(sb.toString())
        } else {
            // the path has no version
            return Pattern.compile(escapeAsRegex(path))
        }
    }

    static final String escapeAsRegex(String path) {
        Objects.requireNonNull(path)
        return path
                .replace('/', "\\/")
                .replace('.', "\\.")
                .replace('\'', "\\'")
                .replace('(', "\\(")
                .replace(')', "\\)")
                .replace('-', "\\-")
                .replace('[', "\\[")
                .replace(']', "\\]")

    }

}
