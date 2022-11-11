package com.kazurayam.materialstore.core.filesystem.metadata;

import com.kazurayam.materialstore.core.filesystem.Jsonifiable;
import com.kazurayam.materialstore.core.filesystem.TemplateReady;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public abstract class IgnoreMetadataKeys implements Iterable<String>, Jsonifiable, TemplateReady {

    public static final IgnoreMetadataKeys NULL_OBJECT = new Builder().build();

    public abstract boolean add(String s);

    public abstract boolean addAll(String... keys);
    public abstract boolean addAll(List<String> collection);

    public abstract int size();

    public abstract Set<String> keySet();

    public abstract boolean contains(String key);

    public abstract Iterator<String> iterator();


    /**
     *
     */
    public static class Builder {

        private Set<String> keySet;

        public Builder() {
            this.keySet = new HashSet<>();
        }

        public Builder ignoreKey(String key) {
            this.keySet.add(key);
            return this;
        }

        public Builder ignoreKeys(String... keys) {
            this.keySet.addAll(Arrays.asList(keys));
            return this;
        }

        public Builder ignoreKeys(List<String> keys) {
            this.keySet.addAll(keys);
            return this;
        }
        public IgnoreMetadataKeys build() {
            return new IgnoreMetadataKeysImpl(keySet);
        }

        public Set<String> getKeySet() {
            return keySet;
        }

        public void setKeySet(Set<String> keySet) {
            this.keySet = keySet;
        }

    }
}
