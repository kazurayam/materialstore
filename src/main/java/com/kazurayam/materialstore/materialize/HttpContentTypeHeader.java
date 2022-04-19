package com.kazurayam.materialstore.materialize;

import org.apache.hc.core5.http.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HttpContentTypeHeader {

    private static Logger logger = LoggerFactory.getLogger(HttpContentTypeHeader.class);

    public static Pattern CONTENT_TYPE_PATTERN =
            Pattern.compile("\\s*([^;]+)(;\\s*(charset=(.+)|boundary=(.*)))?");

    private Header header;

    public HttpContentTypeHeader(Header header) {
        this.header = header;
    }

    public String getMediaType() {
        Matcher m = CONTENT_TYPE_PATTERN.matcher(this.header.getValue());
        if (m.matches()) {
            return m.group(1);
        } else {
            logger.debug("unable to parse the header(name=" +
                    header.getName() + ",value=" + header.getValue() +
                    ") with pattern=" + CONTENT_TYPE_PATTERN.toString());
            return null;
        }
    }

    public String getCharset() {
        Matcher m = CONTENT_TYPE_PATTERN.matcher(this.header.getValue());
        if (m.matches()) {
            return m.group(4);
        } else {
            logger.debug("unable to parse the header(name=" +
                    header.getName() + ",value=" + header.getValue() +
                    ") with pattern=" + CONTENT_TYPE_PATTERN.toString());
            return null;
        }
    }
}
