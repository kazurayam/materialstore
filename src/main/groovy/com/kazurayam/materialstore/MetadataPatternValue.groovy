package com.kazurayam.materialstore

import java.util.regex.Pattern

class MetadataPatternValue implements Comparable {

    private String valueString = null
    private Pattern valuePattern = null

    static MetadataPatternValue of(String key) {
        return new Builder(key).build()
    }

    static MetadataPatternValue of(Pattern key) {
        return new Builder(key).build()
    }

    private MetadataPatternValue(Builder builder) {
        this.valueString = builder.valueString
        this.valuePattern = builder.valuePattern
    }

    boolean isString() {
        return valueString != null
    }

    boolean isPattern() {
        return valuePattern != null
    }

    boolean matches(String subject) {
        if (this.isString()) {
            return subject == valueString
        } else if (this.isPattern()) {
            return valuePattern.matcher(subject).matches()
        } else {
            throw new IllegalStateException()
        }
    }

    //------------ java.lang.Object------------------------------------
    @Override
    boolean equals(Object obj) {
        if (! obj instanceof MetadataPatternValue)
            return false
        MetadataPatternValue other = (MetadataPatternValue)obj
        if (this.isString() && other.isString() &&
                this.valueString == other.valueString) {
            return true
        } else return this.isPattern() && other.isPattern() &&
                this.valuePattern.toString() == other.valuePattern.toString()
    }

    @Override
    int hashCode() {
        if (this.isString()) {
            return this.valueString.hashCode()
        } else if (this.isPattern()) {
            return this.valuePattern.hashCode()
        } else {
            throw new IllegalStateException("is neither of String and Pattern")
        }
    }

    @Override
    String toString() {
        if (this.isString()) {
            return this.valueString
        } else if (this.isPattern()) {
            return "re:" + this.valuePattern.toString()
        } else {
            throw new IllegalStateException("is neither of String and Pattern")
        }
    }


    //---------------- Comparable -------------------------------------
    @Override
    int compareTo(Object obj) {
        if (! obj instanceof MetadataPatternValue) {
            throw new IllegalArgumentException("obj is " + obj.getClass().getName())
        }
        MetadataPatternValue other = (MetadataPatternValue)obj
        if (this.isString() && other.isString()) {
            return this.valueString <=> other.valueString
        } else if (this.isPattern() && other.isPattern()) {
            return this.valuePattern.toString() <=> other.valuePattern.toString()
        } else {
            throw new IllegalStateException("MetadataPatternValue type not match: String and Pattern")
        }
    }

    /**
     *
     */
    static class Builder {
        private String valueString
        private Pattern valuePattern
        Builder(String valueString) {
            Objects.requireNonNull(valueString)
            this.valueString = valueString
        }
        Builder(Pattern valuePattern) {
            Objects.requireNonNull(valuePattern)
            this.valuePattern = valuePattern
        }
        MetadataPatternValue build() {
            return new MetadataPatternValue(this)
        }
    }

}
