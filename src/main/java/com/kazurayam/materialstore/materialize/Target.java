package com.kazurayam.materialstore.materialize;

import com.kazurayam.materialstore.MaterialstoreException;
import org.openqa.selenium.By;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

public class Target {

    private final URL url;
    private final By by;
    private final Map<String, String> parameters;
    private Target(Builder builder) {
        this.url = builder.url;
        this.by = builder.by;
        this.parameters = builder.parameters;
    }
    public URL getUrl() {
        return this.url;
    }
    public By getBy() { return this.by; }
    public Map<String, String> getParameters() { return this.parameters; }
    public Object get(String key) { return this.parameters.get(key); }

    /**
     *
     */
    public static class Builder {
        private final URL url;
        private By by = By.xpath("/html/body");
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
        Builder by(By by) {
            this.by = by;
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
