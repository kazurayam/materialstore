package com.kazurayam.materialstore.metadata


import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.regex.Matcher
import java.util.regex.Pattern

class QueryOnMetadataValue implements Comparable {

    private static final Logger logger = LoggerFactory.getLogger(QueryOnMetadataValue.class)

    private String valueString = null
    private Pattern valuePattern = null

    static QueryOnMetadataValue of(String key) {
        return new Builder(key).build()
    }

    static QueryOnMetadataValue of(Pattern key) {
        return new Builder(key).build()
    }

    private QueryOnMetadataValue(Builder builder) {
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
            //logger.info("subject                      : \"${subject}\"")
            //logger.info("valueString                  : \"${valueString}\"")
            //logger.info("subject == valueString       : \"${subject == valueString}\"")
            //logger.info("SemanticVersionAware.similar : \"${SemanticVersionAwareStringMatcher.similar(subject, valueString)}\"")
            //logger.info("--------------------------------------------")

            if (subject == valueString) {
                return true
            } else {
                SemanticVersionAwareStringMatcher sm = new SemanticVersionAwareStringMatcher(valueString)
                Matcher m = sm.matcher(subject)
                return m.matches()
            }
        } else if (this.isPattern()) {
            return valuePattern.matcher(subject).matches()
        } else {
            throw new IllegalStateException()
        }
    }

    //------------ java.lang.Object------------------------------------
    @Override
    boolean equals(Object obj) {
        if (! obj instanceof QueryOnMetadataValue)
            return false
        QueryOnMetadataValue other = (QueryOnMetadataValue)obj
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
        if (! obj instanceof QueryOnMetadataValue) {
            throw new IllegalArgumentException("obj is " + obj.getClass().getName())
        }
        QueryOnMetadataValue other = (QueryOnMetadataValue)obj
        if (this.isString() && other.isString()) {
            return this.valueString <=> other.valueString
        } else if (this.isPattern() && other.isPattern()) {
            return this.valuePattern.toString() <=> other.valuePattern.toString()
        } else if (this.isString() && other.isPattern()) {
            return -1
        } else {  // this.isPattern() && other.isString()
            return 1
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
        Builder(QueryOnMetadataValue source) {
            Objects.requireNonNull(source)
            this.valueString = source.valueString
            this.valuePattern = source.valuePattern
        }
        QueryOnMetadataValue build() {
            return new QueryOnMetadataValue(this)
        }
    }

}
