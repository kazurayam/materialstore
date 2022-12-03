package com.kazurayam.materialstore.core.filesystem.metadata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class QValue implements Comparable<QValue> {

    private static final Logger logger = LoggerFactory.getLogger(QValue.class);
    private final String valueString;
    private final Pattern valuePattern;

    public static QValue of(String key) {
            return new Builder(key).build();
        }

    public static QValue of(Pattern ptn) {
            return new Builder(ptn).build();
        }

    private QValue(Builder builder) {
        this.valueString = builder.valueString;
        this.valuePattern = builder.valuePattern;
    }

    public boolean isString() {
            return valueString != null;
        }

    public boolean isPattern() {
            return valuePattern != null;
        }

    public boolean matches(String subject) {
        if (this.isString()) {
            //logger.info("subject                      : \"${subject}\"")
            //logger.info("valueString                  : \"${valueString}\"")
            //logger.info("subject == valueString       : \"${subject == valueString}\"")
            //logger.info("SemanticVersionAware.similar : \"${SemanticVersionPattern.similar(subject, valueString)}\"")
            //logger.info("--------------------------------------------")

            if (subject.equals(valueString)) {
                return true;
            } else {
                SemanticVersionPattern sm = new SemanticVersionPattern(valueString);
                Matcher m = sm.matcher(subject);
                return m.matches();
            }

        } else if (this.isPattern()) {
            return valuePattern.matcher(subject).matches();
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof QValue) ) {
            return false;
        }
        QValue other = (QValue) obj;
        if (this.isString() && other.isString() && this.valueString.equals(other.valueString)) {
            return true;
        } else
            return this.isPattern() && other.isPattern() && this.valuePattern.toString().equals(other.valuePattern.toString());
    }

    @Override
    public int hashCode() {
        if (this.isString()) {
            return this.valueString.hashCode();
        } else if (this.isPattern()) {
            return this.valuePattern.hashCode();
        } else {
            throw new IllegalStateException("is neither of String and Pattern");
        }
    }

    @Override
    public String toString() {
        if (this.isString()) {
            return this.valueString;
        } else if (this.isPattern()) {
            return "re:" + this.valuePattern.toString();
        } else {
            throw new IllegalStateException("is neither of String and Pattern");
        }
    }

    @Override
    public int compareTo(QValue other) {
        if (this.isString() && other.isString()) {
            int result = this.valueString.compareTo(other.valueString);
            logger.debug("[compareTo] this.valueString=" + this.valueString +
                    ", other.valueString=" + other.valueString +
                    ", result=" + result);
            return result;
        } else if (this.isPattern() && other.isPattern()) {
            return this.valuePattern.toString().compareTo(other.valuePattern.toString());
        } else if (this.isString() && other.isPattern()) {
            return -1;
        } else {   // this.isPattern() && other.isString()
            return 1;
        }
    }

    /**
     *
     */
    public static class Builder {

        private String valueString;
        private Pattern valuePattern;

        public Builder(String valueString) {
            Objects.requireNonNull(valueString);
            this.valueString = valueString;
            this.valuePattern = null;
        }

        public Builder(Pattern valuePattern) {
            Objects.requireNonNull(valuePattern);
            this.valueString = null;
            this.valuePattern = valuePattern;
        }

        public Builder(QValue source) {
            Objects.requireNonNull(source);
            this.valueString = source.valueString;
            this.valuePattern = source.valuePattern;
        }

        public QValue build() {
                return new QValue(this);
            }
    }
}
