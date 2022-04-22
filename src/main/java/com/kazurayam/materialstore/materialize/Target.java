package com.kazurayam.materialstore.materialize;

import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.Jsonifiable;
import com.kazurayam.materialstore.util.JsonUtil;
import org.openqa.selenium.By;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Target implements Jsonifiable {

    private final URL url;
    private final By by;
    private final Map<String, String> attributes;

    public static Builder builder(URL url) {
        return new Target.Builder(url);
    }

    public static Builder builder(String urlString) throws MaterialstoreException {
        return new Target.Builder(urlString);
    }

    private Target(Builder builder) {
        this.url = builder.url;
        this.by = builder.by;
        this.attributes = builder.attributes;
    }

    public Target(Target source) {
        this.url = source.getUrl();
        this.by = source.getBy();
        this.attributes = new LinkedHashMap<>(source.getAttributes());
    }

    public URL getUrl() {
        return this.url;
    }
    public By getBy() { return this.by; }
    public Target put(String key, String value) {
        this.attributes.put(key, value);
        return this;
    }
    public Target putAll(Map<String, String> attributes) {
        this.attributes.putAll(attributes);
        return this;
    }
    public Map<String, String> getAttributes() { return this.attributes; }
    public Object get(String key) { return this.attributes.get(key); }
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"url\":\"");
        sb.append(JsonUtil.escapeAsJsonString(url.toExternalForm()));
        sb.append("\",");
        sb.append("\"by\":\"");
        sb.append(JsonUtil.escapeAsJsonString(by.toString()));
        sb.append("\",");
        sb.append("\"attributes\":");
        sb.append("{");
        StringBuilder sbAttr = new StringBuilder();
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            if (sbAttr.length() > 0) {
                sbAttr.append(",");
            }
            sbAttr.append("\"");
            sbAttr.append(JsonUtil.escapeAsJsonString(entry.getKey()));
            sbAttr.append("\"");
            sbAttr.append(":");
            sbAttr.append("\"");
            sbAttr.append(JsonUtil.escapeAsJsonString(entry.getValue()));
            sbAttr.append("\"");
        }
        sb.append(sbAttr);
        sb.append("}");
        sb.append("}");
        return sb.toString();
    }
    public String toJson(boolean prettyPrint) {
        if (prettyPrint) {
            return JsonUtil.prettyPrint(toJson(), Map.class);
        } else {
            return toJson();
        }
    }
    /**
     *
     */
    public static class Builder {
        private final URL url;
        private By by = By.xpath("/html/body");
        private final Map<String, String> attributes = new LinkedHashMap<>();
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
            this.attributes.put(key, value);
            return this;
        }
        Builder putAll(Map<String, String> attrs) {
            this.attributes.putAll(attrs);
            return this;
        }
        Target build() {
            return new Target(this);
        }
    }
}
