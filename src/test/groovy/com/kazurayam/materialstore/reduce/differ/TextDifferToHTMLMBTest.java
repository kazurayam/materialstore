package com.kazurayam.materialstore.reduce.differ;

import groovy.xml.MarkupBuilder;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TextDifferToHTMLMBTest {

    @Test
    void test_splitStringWithTags() {
        String OT = TextDifferToHTMLMB.OLD_TAG;
        String NT = TextDifferToHTMLMB.NEW_TAG;
        String given = String.format("  if %sfoo%s is %sbar%s {", OT, OT, NT, NT);
        List<String> expected = Arrays.asList(
                "  if ",
                OT,
                "foo",
                OT,
                " is ",
                NT,
                "bar",
                NT,
                " {"
        );
        assertEquals(expected, TextDifferToHTMLMB.splitStringWithOldNewTags(given));
    }

    @Test
    void test_markupSegments() {
        String OT = TextDifferToHTMLMB.OLD_TAG;
        String NT = TextDifferToHTMLMB.NEW_TAG;
        List<String> segments = Arrays.asList(
                "    <link ", OT, "href=\"https:", OT, "//katalon", OT, "-demo-cura", OT,
                ".", NT, "herokuapp.", NT, "com//css/theme.css\" rel=\"stylesheet\">"
        );
        StringWriter sw = new StringWriter();
        MarkupBuilder mb = new MarkupBuilder(sw);
        // no do it
        TextDifferToHTMLMB.markupSegments(mb, segments);
        String markup = sw.toString();
        assertTrue(markup.contains("<span class=\'deletion\'>"));
        assertTrue(markup.contains("<span class=\'insertion\'>"));
        assertTrue(markup.contains("<span class=\'unchanged\'>"));
        assertTrue(markup.contains("<span class='unchanged'>    &lt;link </span>"));
        assertTrue(markup.contains("<span class='deletion'>href=\"https:</span>"));
        assertTrue(markup.contains("<span class='unchanged'>//katalon</span>"));
        assertTrue(markup.contains("<span class='deletion'>-demo-cura</span>"));
        assertTrue(markup.contains("<span class='unchanged'>.</span>"));
        assertTrue(markup.contains("<span class='insertion'>herokuapp.</span>"));
        assertTrue(markup.contains("<span class='unchanged'>com//css/theme.css\" rel=\"stylesheet\"&gt;</span>"));
    }
}
