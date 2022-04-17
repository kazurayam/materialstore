package com.kazurayam.materialstore.materialize;

import java.net.MalformedURLException;
import java.net.URL;

public class TargetURL {
    private final URL url;
    private final LocatorType locatorType;
    private final String locator;
    private TargetURL(Builder builder) {
        this.url = builder.url;
        this.locatorType = builder.locatorType;
        this.locator = builder.locator;
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
    public static class Builder {
        private final URL url;
        private LocatorType locatorType = LocatorType.XPATH;
        private String locator = "/html/body";
        public Builder(String urlString) throws MalformedURLException {
            this.url = new URL(urlString);
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
        TargetURL build() {
            return new TargetURL(this);
        }
    }
}
