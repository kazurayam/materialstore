package com.kazurayam.materialstore.materialize;

import org.apache.hc.core5.http.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DigestedResponse {

    private static final Logger logger = LoggerFactory.getLogger(DigestedResponse.class);

    private final byte[] content;
    private String mediaType;
    private String charset;

    public DigestedResponse(byte[] content) {
        this.content = content;
        this.mediaType = null;
        this.charset = null;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public void setContentType(Header header) {
        Matcher m = CONTENT_TYPE_PATTERN.matcher(header.getValue());
        if (m.matches()) {
            this.setMediaType(m.group(1));
            if (m.group(4) != null) {
                this.setCharset(m.group(4));
            }
        } else {
            logger.warn("unable to parse the header(name=" +
                    header.getName() + ",value=" + header.getValue() +
                    ") with pattern=" + CONTENT_TYPE_PATTERN.toString());
        }
    }

    public byte[] getContent() {
        return this.content;
    }

    public String getMediaType() {
        return this.mediaType;
    }

    public String getCharset() {
        return this.charset;
    }

    public static final Pattern CONTENT_TYPE_PATTERN =
            Pattern.compile("\\s*([^;]+)(;\\s*(charset=(.+)|boundary=(.*)))?");
}
