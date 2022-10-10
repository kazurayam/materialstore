package com.kazurayam.materialstore.filesystem;

import com.kazurayam.materialstore.filesystem.metadata.IgnoreMetadataKeys;
import com.kazurayam.materialstore.filesystem.metadata.QEntry;
import com.kazurayam.materialstore.filesystem.metadata.QValue;
import com.kazurayam.materialstore.filesystem.metadata.QueryDescription;
import com.kazurayam.materialstore.filesystem.metadata.QueryOnMetadataImpl;
import com.kazurayam.materialstore.filesystem.metadata.SortKeys;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public abstract class QueryOnMetadata implements Jsonifiable, TemplateReady {

    private SortKeys sortKeys = new SortKeys();

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(Map<String, String> source) {
        return new Builder(source);
    }

    public static Builder builder(Metadata metadata) {
        return new Builder(metadata);
    }

    public static Builder builder(Metadata metadata, IgnoreMetadataKeys ignoreMetadataKeys) {
        return new Builder(metadata, ignoreMetadataKeys);
    }

    public static Builder builder(QueryOnMetadata source) {
        return new Builder(source);
    }

    public abstract boolean containsKey(String key);

    public abstract Set<QEntry> entrySet();

    public abstract QValue get(String key);

    public SortKeys getSortKeys() {
        return sortKeys;
    }

    /**
     * string representation of this QueryOnMetadata,
     * the keys are sorted by the given SortKeys specification
     *
     */
    public abstract QueryDescription getQueryDescription();

    public abstract String getAsString(String key);

    public abstract boolean isEmpty();

    public abstract Set<String> keySet();

    public abstract boolean matches(Metadata metadata);

    public abstract int size();

    public QueryOnMetadata sortKeys(SortKeys sortKeys) {
        this.sortKeys = sortKeys;
        return this;
    }

    public abstract List<Map<String, String>> toJSONTextTokens();

    public static final QueryOnMetadata NULL_OBJECT = new Builder().build();
    public static final QueryOnMetadata ANY = new Builder().put("*", Pattern.compile(".*")).build();

    /**
     *
     */
    public static class Builder {

        private final Map<String, QValue> query;

        public Builder() {
            this.query = new HashMap<>();
        }

        public Builder(final Map<String, String> map) {
            this();
            Objects.requireNonNull(map);
            for (String key : map.keySet()) {
                query.put(key, QValue.of(map.get(key)));
            }
        }

        public Builder(Metadata source) {
            this(source, IgnoreMetadataKeys.NULL_OBJECT);
        }

        public Builder(final Metadata source,
                       final IgnoreMetadataKeys ignoreMetadataKeys) {
            this();
            Objects.requireNonNull(ignoreMetadataKeys);
            Objects.requireNonNull(source);
            for (String key : source.keySet()) {
                QValue mpv = QValue.of(source.get(key));
                if (!ignoreMetadataKeys.contains(key)) {
                    query.put(key, mpv);
                }
            }
        }

        public Builder(final QueryOnMetadata source) {
            this();
            Objects.requireNonNull(source);
            for (String key : source.keySet()) {
                QValue mpv = new QValue.Builder(source.get(key)).build();
                query.put(key, mpv);
            }
        }

        public Builder put(String key, String value) {
            query.put(key, QValue.of(value));
            return this;
        }

        public Builder put(String key, Pattern value) {
            query.put(key, QValue.of(value));
            return this;
        }

        public QueryOnMetadata build() {
            return new QueryOnMetadataImpl(query);
        }
    }
}
