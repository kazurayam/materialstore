package com.kazurayam.materialstore.materialize;

import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;
import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpHeaderContentTypeTest {

    @Test
    void test_CONTENT_TYPE_PATTERN_text() {
        Matcher m = HttpHeaderContentType.CONTENT_TYPE_PATTERN.matcher("text/html; charset=UTF-8");
        assertTrue(m.matches());
        assertEquals(5, m.groupCount());
        assertEquals("text/html", m.group(1));
        assertEquals("; charset=UTF-8", m.group(2));
        assertEquals("charset=UTF-8", m.group(3));
        assertEquals("UTF-8", m.group(4));
        assertNull(m.group(5));
    }

    @Test
    void test_CONTENT_TYPE_PATTERN_multipart() {
        Matcher m = HttpHeaderContentType.CONTENT_TYPE_PATTERN.matcher("multipart/form-data; boundary=something");
        assertTrue(m.matches());
        assertEquals(5, m.groupCount());
        assertEquals("multipart/form-data", m.group(1));
        assertEquals("boundary=something", m.group(3));
        assertNull(m.group(4));
        assertEquals("something", m.group(5));
    }

    @Test
    public void test_getMediaType() {
        Header contentType = new BasicHeader("ContentType", "application/javascript; charset=UTF-8");
        HttpHeaderContentType header = new HttpHeaderContentType(contentType);
        assertEquals("application/javascript", header.getMediaType());
    }

    @Test
    public void test_getCharset() {
        Header contentType = new BasicHeader("ContentType", "application/javascript; charset=UTF-8");
        HttpHeaderContentType header = new HttpHeaderContentType(contentType);
        assertEquals("UTF-8", header.getCharset());
    }

}
