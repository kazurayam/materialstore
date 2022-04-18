package com.kazurayam.materialstore.materialize;

import com.kazurayam.materialstore.MaterialstoreException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class TargetURL {
    private final URL url;
    private final LocatorType locatorType;
    private final String locator;
    private final Map<String, String> metadata;
    private TargetURL(Builder builder) {
        this.url = builder.url;
        this.locatorType = builder.locatorType;
        this.locator = builder.locator;
        this.metadata = builder.metadata;
    }
    public URL getUrl() {
        return this.url;
    }
    public LocatorType getLocatorType() {
        return this.locatorType;
    }
    public String getLocator() {
        return this.locator;
    }
    public Map<String, String> getMetadata() { return this.metadata; }

    /**
     *
     */
    public static class Builder {
        private final URL url;
        private LocatorType locatorType = LocatorType.XPATH;
        private String locator = "/html/body";
        private Map<String, String> metadata;
        public Builder(String urlString) throws MaterialstoreException {
            try {
                this.url = new URL(urlString);
            } catch (MalformedURLException e) {
                throw new MaterialstoreException(e);
            }
        }
        public Builder(URL url) {
            this.url = url;
        }
        Builder locatorType(LocatorType locatorType) {
            this.locatorType = locatorType;
            return this;
        }
        Builder locator(String locator) {
            this.locator = locator;
            return this;
        }
        Builder putMetadata(String key, String value) {
            this.metadata.put(key, value);
            return this;
        }
        TargetURL build() {
            return new TargetURL(this);
        }
    }
}
