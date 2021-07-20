package com.kazurayam.materialstore.misc

import com.kazurayam.materialstore.diff.differ.TextDifferToHTML
import groovy.xml.MarkupBuilder
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

import java.util.regex.Matcher
import java.util.regex.Pattern

import static org.junit.jupiter.api.Assertions.*

/**
 * This test class does 2 things.
 *
 * 1. reproduces a problem that automatic "pretty-printing" by MarkupBuilder causes.
 * 2. proposed that `public void setNospace(boolean nospace)` can fix that problem.
 */
@Disabled
class MarkupBuilderSetNospaceTest {

    String markup_by_MarkupBuilder_as_is(String given) {
        List<String> segments = divideStringIntoSegments(given)
        StringWriter sw = new StringWriter()
        MarkupBuilder mb = new MarkupBuilder(sw)
        mb.span(class:"line") {
            segments.each { String segment ->
                span(segment)
            }
        }
        sw.toString()
    }

    String markup_by_MarkupBuilder_modified(String given) {
        List<String> segments = divideStringIntoSegments(given)
        StringWriter sw = new StringWriter()
        MarkupBuilder mb = new MarkupBuilder(sw)
        mb.span(class:"line") {
            segments.each { String segment ->
                mb.metaClass.setAttribute(mb, "nospace", true)
                mb.span(segment)
            }
        }
        sw.toString()
    }



    Pattern SPANNING_PATTERN = Pattern.compile("\\s*\\S+")

    List<String> divideStringIntoSegments(String line, clazz="pl") {
        Matcher m = SPANNING_PATTERN.matcher(line)
        List<String> segments = m.findAll()
        return segments
    }

    /**
     *
     */
    @Test
    void test_markup_by_MarkupBuilder_as_is() {
        String given = "    if (! obj instanceof Material) {"
        String expected = "<span class=\"line\"><span>    if</span><span> (!</span><span> obj</span><span> instanceof</span><span> Material)</span><span> {</span></span>"
        String actual = markup_by_MarkupBuilder_as_is(given)
        assertNotEquals(expected, actual)
    }

    /**
     *
     */
    @Test
    void test_markup_by_MarkupBuilder_modified() {
        String given = "    if (! obj instanceof Material) {"
        String expected = "<span class=\"line\"><span>    if</span><span> (!</span><span> obj</span><span> instanceof</span><span> Material)</span><span> {</span></span>"
        String actual = markup_by_MarkupBuilder_modified(given)
        assertEquals(expected, actual)
        /*
        expected: <<span class="line"><span>    if</span><span> (!</span><span> obj</span><span> instanceof</span><span> Material)</span><span> {</span></span>>
         but was: <<span class='line'><span>    if</span><span> (!</span><span> obj</span><span> instanceof</span><span> Material)</span><span> {</span>
         */
    }

    /**
     *
     */
    @Test
    void test_divideStringIntoSegments() {
        String given = "    if (! obj instanceof Material) {"
        List<String> expected = [
                "    if",
                " (!",
                " obj",
                " instanceof",
                " Material)",
                " {"
        ]
        assertEquals(expected, TextDifferToHTML.divideStringIntoSegments(given))
    }


}
