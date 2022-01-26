package com.kazurayam.materialstore

import org.junit.jupiter.api.Test

import java.util.regex.Matcher
import java.util.regex.Pattern

class SemanticVersionAwareStringMatcherTest {

    private final String left  = "https://cdn.jsdelivr.net/npm/bootstrap@5.1.0/dist/js/bootstrap.bundle.min.js"
    private final String right = "https://cdn.jsdelivr.net/npm/bootstrap@5.1.3-rc1/dist/js/bootstrap.bundle.min.js"

    @Test
    void test_escapeAsRegex() {
        assert SemanticVersionAwareStringMatcher.escapeAsRegex(
                '/npm/bootstrap@') == '\\/npm\\/bootstrap@'
        assert SemanticVersionAwareStringMatcher.escapeAsRegex(
                '/dist/js/bootstrap.bundle.min.js') == '\\/dist\\/js\\/bootstrap\\.bundle\\.min\\.js'
    }

    @Test
    void test_REGEX_VERSION_valid() {
        List<String> validVersions = [ '5.1.3', '5.1.5-rc', '50.11.38' ]
        Pattern p = Pattern.compile('^' + SemanticVersionAwareStringMatcher.REGEX_VERSION + '$')
        validVersions.each { it ->
            Matcher m = p.matcher(it)
            assert m.matches() : "input = ${it}"
        }
    }

    @Test
    void test_REGEX_VERSION_invalid() {
        List<String> invalidVersions = ['1.0', '1-2-3', '4_5_6', '1.0.0.2', '2.5X']
        Pattern p = Pattern.compile('^' + SemanticVersionAwareStringMatcher.REGEX_VERSION + '$')
        invalidVersions.each { it ->
            Matcher m = p.matcher(it)
            assert ! m.matches(): "input = ${it}"
        }
    }


    @Test
    void test_similar() {
        assert SemanticVersionAwareStringMatcher.similar(left, right)
    }

    /**
     * The character '[' and ']' in the string were problematic
     */
    @Test
    void test_similar_edge_case() {
        String str = "//a[@id='btn-make-appointment']"
        assert SemanticVersionAwareStringMatcher.similar(str, str)
    }

    private List pathFixtures = [
            "/npm/bootstrap-icons@1.5.0/font/bootstrap-icons.css",
            "/npm/bootstrap-icons@1.5.0/font/fonts/bootstrap-icons.woff2",
            "/npm/bootstrap@5.1.0/dist/css/bootstrap.min.css",
            "/npm/bootstrap@5.1.0/dist/js/bootstrap.bundle.min.js",
            "/ajax/libs/jquery/1.12.4/jquery.min.js",
            "/",
            "/umineko-1960x1960.jpg"
    ]

    @Test
    void test_translatePathToRegex() {
        pathFixtures.each { path ->
            Pattern p = SemanticVersionAwareStringMatcher.translatePathToRegex(path)
            Matcher m = p.matcher(path)
            assert m.matches()
        }
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
    void test_translatePathToRegex_and_apply(){
        URL rightUrl = new URL(right)
        String regex = SemanticVersionAwareStringMatcher.translatePathToRegex(rightUrl.getPath())
        Pattern p = Pattern.compile(regex)
        URL leftUrl = new URL(left)
        Matcher m = p.matcher(leftUrl.getPath())
        assert m.matches()
    }

    @Test
    void test_VERSIONED_PATH_PARSER() {
        URL url = new URL(right)
        String path = url.getPath()
        Matcher m = SemanticVersionAwareStringMatcher.VERSIONED_PATH_PARSER.matcher(path)
        assert m.matches()
        assert m.groupCount() == 4
        assert m.group(1) == "/npm/bootstrap@"
        assert m.group(2) == "5.1.3-rc1"
        assert m.group(3) == "-rc1"
        assert m.group(4) == "/dist/js/bootstrap.bundle.min.js"
    }

}
