package com.kazurayam.materialstore.materialize;

import com.kazurayam.materialstore.MaterialstoreException;
import org.openqa.selenium.By;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

public class Target {

    private final URL url;
    private final LocatorType locatorType;
    private final String locator;
    private final Map<String, String> parameters;
    private Target(Builder builder) {
        this.url = builder.url;
        this.locatorType = builder.locatorType;
        this.locator = builder.locator;
        this.parameters = builder.parameters;
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
    public By getBy() {
        return (this.getLocatorType().equals(LocatorType.XPATH)) ?
            By.xpath(this.getLocator()) : By.cssSelector(this.getLocator());
    }
    public Map<String, String> getParameters() { return this.parameters; }
    public Object get(String key) { return this.parameters.get(key); }

    /**
     *
     */
    public static class Builder {
        private final URL url;
        private LocatorType locatorType = LocatorType.XPATH;
        private String locator = "/html/body";
        private Map<String, String> parameters = new LinkedHashMap<>();
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
        Builder put(String key, String value) {
            this.parameters.put(key, value);
            return this;
        }
        Target build() {
            return new Target(this);
        }
    }
}
