package com.kazurayam.materialstore.core.util;

import java.util.Objects;

public class KeyValuePair implements Comparable<KeyValuePair> {
    private String key;
    private String value;
    public KeyValuePair(String key, String value) {
        Objects.requireNonNull(key, "key must not be null");
        // value can be null
        this.key = key;
        this.value = value;
    }
    public String getKey() { return key; }
    public String getValue() { return value; }
    @Override
    public String toString() {
        return key + "=" + value;
    }
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof KeyValuePair) {
            KeyValuePair other = (KeyValuePair)obj;
            if (this.compareTo(other) == 0) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + this.key.hashCode();
        hash = 31 * hash + this.value.hashCode();
        return hash;
    }
    @Override
    public int compareTo(KeyValuePair other) {
        int keyComparisonResult = this.key.compareTo(other.key);
        if (keyComparisonResult == 0) {
            return this.value.compareTo(other.value);
        } else {
            return keyComparisonResult;
        }
    }
}

