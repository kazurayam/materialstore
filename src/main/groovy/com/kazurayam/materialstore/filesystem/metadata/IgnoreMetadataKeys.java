package com.kazurayam.materialstore.filesystem.metadata;

import com.kazurayam.materialstore.filesystem.Jsonifiable;
import com.kazurayam.materialstore.filesystem.TemplateReady;
import groovy.lang.Closure;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public abstract class IgnoreMetadataKeys implements Iterable<String>, Jsonifiable, TemplateReady {

    public static final IgnoreMetadataKeys NULL_OBJECT = new Builder().build();

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
            this.keySet = new HashSet<String>();
        }

        public Builder ignoreKey(String key) {
            this.keySet.add(key);
            return this;
        }

        public Builder ignoreKeys(String... keys) {
            DefaultGroovyMethods.each(keys, new Closure<Boolean>(this, this) {
                public Boolean doCall(String it) {
                    return Builder.this.getKeySet().add(it);
                }

                public Boolean doCall() {
                    return doCall(null);
                }

            });
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
