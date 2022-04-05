package com.kazurayam.materialstore.filesystem.metadata;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SemanticVersionPatternTest {

    private final String left = "https://cdn.jsdelivr.net/npm/bootstrap@5.1.0/dist/js/bootstrap.bundle.min.js";
    private final String right = "https://cdn.jsdelivr.net/npm/bootstrap@5.1.3-rc1/dist/js/bootstrap.bundle.min.js";
    private final List<String> pathFixtures = new ArrayList<>(
            Arrays.asList("/npm/bootstrap-icons@1.5.0/font/bootstrap-icons.css",
                    "/npm/bootstrap-icons@1.5.0/font/fonts/bootstrap-icons.woff2",
                    "/npm/bootstrap@5.1.0/dist/css/bootstrap.min.css",
                    "/npm/bootstrap@5.1.0/dist/js/bootstrap.bundle.min.js",
                    "/ajax/libs/jquery/1.12.4/jquery.min.js",
                    "/",
                    "/umineko-1960x1960.jpg"));

    @Test
    public void test_escapeAsRegex() {
        assert SemanticVersionPattern.escapeAsRegex("/npm/bootstrap@").equals("\\/npm\\/bootstrap@");
        assert SemanticVersionPattern.escapeAsRegex("/dist/js/bootstrap.bundle.min.js").equals("\\/dist\\/js\\/bootstrap\\.bundle\\.min\\.js");
    }

    @Test
    public void test_REGEX_VERSION_valid() {
        List<String> validVersions = new ArrayList<String>(Arrays.asList("5.1.3", "5.1.5-rc", "50.11.38"));
        final Pattern p = Pattern.compile("^" + SemanticVersionPattern.REGEX_VERSION + "$");
        validVersions.forEach( it -> {
            Matcher m = p.matcher((CharSequence) it);
            assert m.matches() : "input = " + it;
        });
    }

    @Test
    public void test_REGEX_VERSION_invalid() {
        List<String> invalidVersions = new ArrayList<String>(Arrays.asList("1.0", "1-2-3", "4_5_6", "1.0.0.2", "2.5X"));
        final Pattern p = Pattern.compile("^" + SemanticVersionPattern.REGEX_VERSION + "$");
        invalidVersions.forEach( it -> {
            Matcher m = p.matcher((CharSequence) it);
            assert !m.matches() : "input = " + it;
        });
    }

    @Test
    public void test_pattern() {
        Pattern p = new SemanticVersionPattern(right).pattern();
        System.out.println(p.toString());
    }

    @Test
    public void test_matcher() {
        Matcher m = new SemanticVersionPattern(right).matcher(left);
        assert m.matches();
    }

    @Test
    public void test_straightMatcher_truthy() {
        Matcher m = SemanticVersionPattern.straightMatcher("abcd/1.2.3-beta/xyz");
        Assertions.assertTrue(m.matches());
        Assertions.assertEquals("abcd/", m.group(1));
        Assertions.assertEquals("1.2.3-beta", m.group(2));
        Assertions.assertEquals("-beta", m.group(3));
        Assertions.assertEquals("/xyz", m.group(4));
    }

    @Test
    public void test_straightMatcher_falsy() {
        Matcher m = SemanticVersionPattern.straightMatcher("abcd/efg/xyz");
        Assertions.assertFalse(m.matches());
    }

    /**
     * The character '[' and ']' in the string were problematic
     */
    @Test
    public void test_similar_edge_case() {
        String strLeft = "//a[@id='btn-make-appointment']";
        String strRight = strLeft;
        Matcher m = new SemanticVersionPattern(strRight).matcher(strLeft);
        assert m.matches();
    }

    @Test
    public void test_translatePathToRegex() {
        pathFixtures.forEach( path -> {
            Pattern p = SemanticVersionPattern.translateToBaseStrToPattern((String) path);
            Matcher m = p.matcher((CharSequence) path);
            assert m.matches();
        });
    }

    /**
     * test the following sequence
     * 1. 2 URLs are given; both contains a semantic version but slightly different.
     * - 5.1.0
     * - 5.1.3-rc1
     * 2. translate the path part of the 1st URL into a regex
     * 3. instantiate a Pattern object with the regex
     * 4. instantiate a Matcher object with the path part of the 2nd URL
     * 5. do match() to see if the regex generated out of the 1st URL matches the the 2nd URL
     */
    @Test
    public void test_translateToBaseStrToPattern() throws MalformedURLException {
        URL rightUrl = new URL(right);
        Pattern p = SemanticVersionPattern.translateToBaseStrToPattern(rightUrl.getPath());
        URL leftUrl = new URL(left);
        Matcher m = p.matcher(leftUrl.getPath());
        assert m.matches();
    }

    @Test
    public void test_VERSIONED_PATH_PARSER() throws MalformedURLException {
        URL url = new URL(right);
        String path = url.getPath();
        Matcher m = SemanticVersionPattern.VERSIONED_PATH_PARSER.matcher(path);
        assert m.matches();
        assert m.groupCount() == 4;
        assert m.group(1).equals("/npm/bootstrap@");
        assert m.group(2).equals("5.1.3-rc1");
        assert m.group(3).equals("-rc1");
        assert m.group(4).equals("/dist/js/bootstrap.bundle.min.js");
    }

}
