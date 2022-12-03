package com.kazurayam.materialstore.base.materialize;

import com.google.common.collect.ImmutableMap;
import com.kazurayam.materialstore.core.filesystem.Jsonifiable;
import com.kazurayam.materialstore.core.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.core.util.JsonUtil;
import org.openqa.selenium.By;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A Target object is immutable.
 */
public final class Target implements Jsonifiable {

    private final URL url;
    private final By handle;
    private final ImmutableMap<String, String> attributes;

    public static Builder builder(URL url) {
        return new Builder(url);
    }

    public static Builder builder(String urlString) throws MaterialstoreException {
        return new Builder(urlString);
    }

    /**
     * primary constructor is private, takes the Builder instance
     */
    private Target(Builder builder) {
        this.url = builder.url;
        this.handle = builder.handle;
        this.attributes =
                ImmutableMap.<String, String>builder()
                        .putAll(builder.attributes)
                        .build();
    }

    /*
     * copy constructor
     */
    public Target(Target source) {
        this.url = source.getUrl();
        this.handle = source.getHandle();
        this.attributes = source.getAttributes();
    }

    /*
     * creates a new instance of Target class while replacing the By with specified value
     */
    public Target copyWith(By handle) {
        return Target.builder(this.getUrl())
                .handle(handle)
                .putAll(this.getAttributes())
                .build();
    }

    /*
     * creates a new instance of Target class while adding
     * a new attribute with kew=value.
     */
    public Target copyWith(String key, String value) {
        return Target.builder(this.getUrl())
                .handle(this.getHandle())
                .putAll(this.getAttributes())
                .put(key, value)
                .build();
    }

    /*
     * creates a new instance of Target class while adding attributes specified
     */
    public Target copyWith(Map<String, String> newAttributes) {
        return Target.builder(this.getUrl())
                .handle(this.getHandle())
                .putAll(this.getAttributes())
                .putAll(newAttributes)
                .build();
    }

    public URL getUrl() {
        return this.url;
    }
    public By getHandle() { return this.handle; }
    public ImmutableMap<String, String> getAttributes() { return this.attributes; }
    public Object get(String key) { return this.attributes.get(key); }

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"url\":\"");
        sb.append(JsonUtil.escapeAsJsonString(url.toExternalForm()));
        sb.append("\",");
        sb.append("\"handle\":\"");
        sb.append(JsonUtil.escapeAsJsonString(handle.toString()));
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
        private By handle = By.xpath("/html/body");
        private Map<String, String> attributes = new LinkedHashMap<>();
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
        public Builder handle(By handle) {
            this.handle = handle;
            return this;
        }
        public Builder put(String key, String value) {
            this.attributes.put(key, value);
            return this;
        }
        public Builder putAll(Map<String, String> attrs) {
            this.attributes.putAll(attrs);
            return this;
        }
        public Target build() {
            return new Target(this);
        }
    }
}
