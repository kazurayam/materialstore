package com.kazurayam.materialstore.core;

import com.kazurayam.materialstore.core.metadata.IdentifyMetadataValues;
import com.kazurayam.materialstore.core.metadata.IgnoreMetadataKeys;
import com.kazurayam.materialstore.core.metadata.MetadataAttribute;
import com.kazurayam.materialstore.core.metadata.MetadataIdentification;
import com.kazurayam.materialstore.core.metadata.MetadataImpl;
import com.kazurayam.materialstore.util.KeyValuePair;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public abstract class Metadata implements Comparable<Metadata>, Jsonifiable, TemplateReady {
    public abstract void annotate(QueryOnMetadata query);

    public abstract void annotate(QueryOnMetadata query, IgnoreMetadataKeys ignoreMetadataKeys, IdentifyMetadataValues identifyMetadataValues);

    public abstract boolean canBeIdentified(String key, IdentifyMetadataValues identifyMetadataValues);

    public abstract boolean canBePaired(QueryOnMetadata query, String key);

    public abstract boolean containsKey(String key);

    public abstract boolean containsCategoryDiff();

    public abstract String get(String key);

    public abstract MaterialLocator getMaterialLocatorLeft();

    public abstract MaterialLocator getMaterialLocatorRight();

    public abstract MetadataIdentification getMetadataIdentification();

    public abstract MetadataIdentification getMetadataIdentification(SortKeys sortKeys);

    public abstract MetadataAttribute getMetadataAttribute(String key);

    public abstract boolean isEmpty();

    public abstract Set<String> keySet();

    public abstract boolean matchesByAster(QueryOnMetadata query, String key);

    public abstract boolean matchesIndividually(QueryOnMetadata query, String key);

    public abstract int size();

    public abstract String toURLAsString() throws MaterialstoreException;

    public abstract URL toURL() throws MaterialstoreException;

    public abstract int compareTo(Metadata other);

    public static Builder builder() {
        return new Builder();
    }

    /*
     * with deep copy
     */
    public static Builder builder(Metadata source) {
        return new Builder(source);
    }

    public static Builder builder(URL url) {
        return new Builder(url);
    }

    public static Builder builder(Map<String, String> map) {
        return new Builder(map);
    }

    public static final Metadata NULL_OBJECT = new Builder().build();
    public static final String KEY_URL_PROTOCOL = "URL.protocol";
    public static final String KEY_URL_PORT = "URL.port";
    public static final String KEY_URL_HOST = "URL.host";
    public static final String KEY_URL_PATH = "URL.path";
    public static final String KEY_URL_QUERY = "URL.query";
    public static final String KEY_URL_FRAGMENT = "URL.fragment";

    /**
     *
     */
    public static class Builder {
        public Builder() {
            attributes = new LinkedHashMap<>();
        }

        public Builder(Metadata source) {
            this();
            for (String key : source.keySet()) {
                attributes.put(key, source.getMetadataAttribute(key));
            }
        }

        public Builder(Map<String, String> map) {
            this();
            for (String key : map.keySet()) {
                MetadataAttribute attribute = new MetadataAttribute(key, map.get(key));
                attributes.put(key, attribute);
            }
        }

        public Builder(URL url) {
            this();
            Objects.requireNonNull(url);
            this.url = url;
            attributes.put(KEY_URL_PROTOCOL, new MetadataAttribute(KEY_URL_PROTOCOL, url.getProtocol()));
            if (url.getProtocol().startsWith("http")) {
                if (url.getPort() < 0) {
                    attributes.put(KEY_URL_PORT, new MetadataAttribute(KEY_URL_PORT, "80"));
                } else {
                    attributes.put(KEY_URL_PORT, new MetadataAttribute(KEY_URL_PORT, Integer.valueOf(url.getPort()).toString()));
                }
            }

            attributes.put(KEY_URL_HOST, new MetadataAttribute(KEY_URL_HOST, url.getHost()));
            if (url.getPath() != null) {
                attributes.put(KEY_URL_PATH, new MetadataAttribute(KEY_URL_PATH, url.getPath()));
            }

            if (url.getQuery() != null) {
                //attributes.put(KEY_URL_QUERY, new MetadataAttribute(KEY_URL_QUERY, url.getQuery()));
                List<KeyValuePair> pairs = parseQuery(url.getQuery());
                pairs.forEach((KeyValuePair kvp) -> {
                    String k = KEY_URL_QUERY + "?" + kvp.getKey();
                    String v = (kvp.getValue() != null) ? kvp.getValue() : "null";
                    attributes.put(k, new MetadataAttribute(k, v));
                });
            }

            int posHash = url.toString().indexOf("#");
            if (posHash >= 0) {
                attributes.put(KEY_URL_FRAGMENT, new MetadataAttribute(KEY_URL_FRAGMENT, url.toString().substring(posHash + 1)));
            }

        }

        public Builder put(String key, String value) {
            Objects.requireNonNull(key);
            attributes.put(key, new MetadataAttribute(key, value));
            return this;
        }

        public Builder putAll(Map<String, String> m) {
            Objects.requireNonNull(m);
            for (String key : m.keySet()) {
                attributes.put(key, new MetadataAttribute(key, m.get(key)));
            }
            return this;
        }

        public Builder exclude(String... keys) {
            Arrays.asList(keys).forEach((String key) -> {
                    excludedKeys.add(key);
            });
            return this;
        }

        public Metadata build() {
            excludedKeys.forEach((String key) -> {
                attributes.remove(key);
            });
            MetadataImpl impl = new MetadataImpl(attributes);
            impl.setURL(this.url);
            return (Metadata)impl;
        }

        public Map<String, MetadataAttribute> getAttributes() {
            return attributes;
        }

        public void setAttributes(Map<String, MetadataAttribute> attributes) {
            this.attributes = attributes;
        }

        private Map<String, MetadataAttribute> attributes;

        private List<String> excludedKeys = new ArrayList<>();

        private URL url = null;
    }

    /*
     *
     * @param query "a=x&b=y&c"
     * @return [new KeyValuePair("a","x"), new KeyValuePair("b","y"), new KeyValuePair("c, null)]
     */
    public static List<KeyValuePair> parseQuery(String query) {
        Objects.requireNonNull(query);
        List<KeyValuePair> list = new ArrayList<>();
        String[] pairs = query.split("&");
        for (int i = 0; i < pairs.length; i++) {
            String[] kv = pairs[i].split("=");
            String key = kv[0];
            String value = null;
            if (kv.length == 2) {
                value = kv[1];
            }
            list.add(new KeyValuePair(key, value));
        }
        return list;
    }
}
