package com.kazurayam.materialstore.filesystem


import com.kazurayam.materialstore.filesystem.metadata.IgnoreMetadataKeys
import com.kazurayam.materialstore.filesystem.metadata.QueryOnMetadataImpl
import com.kazurayam.materialstore.filesystem.metadata.SemanticVersionAwareStringMatcher
import com.kazurayam.materialstore.filesystem.metadata.SortKeys
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.regex.Matcher
import java.util.regex.Pattern

abstract class QueryOnMetadata implements JSONifiable, TemplateReady {

    public static final QueryOnMetadata NULL_OBJECT =
            new Builder().build()

    public static final QueryOnMetadata ANY =
            new Builder().put(
                    "*",
                    Pattern.compile(".*")
            ).build()

    //-------------------------factory methods --------------------------------

    static Builder builder() {
        return new Builder()
    }

    static Builder builder(Map<String, String> source) {
        return new Builder(source)
    }

    static Builder builder(Metadata metadata) {
        return new Builder(metadata)
    }

    static Builder builder(Metadata metadata,
                           IgnoreMetadataKeys ignoreMetadataKeys) {
        return new Builder(metadata, ignoreMetadataKeys)
    }

    static Builder builder(QueryOnMetadata source) {
        return new Builder(source)
    }

    //-----------------------------------------------------------------

    abstract boolean containsKey(String key)

    abstract Set<Entry> entrySet()

    abstract QValue get(String key)

    /**
     * string representation of this QueryOnMetadata,
     * the keys are sorted by the given SortKeys specification
     *
     * @param sortKeys
     * @return
     */
    abstract String getDescription(SortKeys sortKeys)

    abstract String getAsString(String key)

    abstract boolean isEmpty()

    abstract Set<String> keySet()

    abstract boolean matches(Metadata metadata)

    abstract int size()

    abstract List<Map<String,String>> toJSONTextTokens()

    /**
     *
     */
    public static class Builder {

        private Map<String, QValue> query

        Builder() {
            this.query = new HashMap<String, QValue>()
        }

        Builder(Map<String, String> map) {
            this()
            Objects.requireNonNull(map)
            map.keySet().each { key ->
                query.put(
                        key,
                        QValue.of(map.get(key))
                )
            }
        }
        Builder(Metadata source) {
            this(source, IgnoreMetadataKeys.NULL_OBJECT)
        }
        Builder(Metadata source,
                IgnoreMetadataKeys ignoreMetadataKeys) {
            this()
            Objects.requireNonNull(ignoreMetadataKeys)
            Objects.requireNonNull(source)
            //
            source.keySet().each {key ->
                QValue mpv = QValue.of((String)source.get(key))
                if (!ignoreMetadataKeys.contains(key)) {
                    query.put(key, mpv)
                }
            }
        }
        Builder(QueryOnMetadata source) {
            this()
            Objects.requireNonNull(source)
            source.keySet().each {key ->
                QValue mpv = new QValue.Builder(source.get(key)).build()
                query.put(key, mpv)
            }
        }
        Builder put(String key, String value) {
            query.put(
                    key,
                    QValue.of(value)
            )
            return this
        }
        Builder put(String key, Pattern value) {
            query.put(
                    key,
                    QValue.of(value)
            )
            return this
        }
        QueryOnMetadata build() {
            return new QueryOnMetadataImpl(query)
        }
    }

    /**
     * a pair of Key-Value in the QueryOnMetadata object.
     * This class implements boolean matches(Metadata) method, which works
     * as a helper for QueryOnMetadataImpl#matches(Metadata) method.
     */
    static class Entry implements Comparable {
        private String key
        private QValue query
        Entry(String key, QValue qValue) {
            this.key = key
            this.query = qValue
        }
        String getKey() {
            return this.key
        }
        QValue getQValue() {
            return this.query
        }
        /**
         *
         * @param metadata
         * @return
         */
        boolean matches(Metadata metadata) {
            if (this.key == "*") {
                boolean found = false
                metadata.keySet().each {metadataKey ->
                    if (this.QValue.matches(metadata.get(metadataKey))) {
                        found = true
                    }
                }
                return found
            } else if (metadata.containsKey(key)) {
                return this.query.matches(metadata.get(key))
            } else {
                return false
            }
        }
        @Override
        int compareTo(Object obj) {
            if (!obj instanceof Entry) {
                throw new IllegalArgumentException("obj is not Entry")
            }
            Entry other = (Entry)obj
            def keyComp = this.key <=> other.key
            if (keyComp != 0) {
                return this.query <=> other.QValue
            } else
                return keyComp
        }
    }

    static class QValue implements Comparable {

        private static final Logger logger = LoggerFactory.getLogger(QValue.class)

        private String valueString = null
        private Pattern valuePattern = null

        static QValue of(String key) {
            return new Builder(key).build()
        }

        static QValue of(Pattern key) {
            return new Builder(key).build()
        }

        private QValue(Builder builder) {
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
            if (! obj instanceof QValue)
                return false
            QValue other = (QValue)obj
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
            if (! obj instanceof QValue) {
                throw new IllegalArgumentException("obj is " + obj.getClass().getName())
            }
            QValue other = (QValue)obj
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
            Builder(QValue source) {
                Objects.requireNonNull(source)
                this.valueString = source.valueString
                this.valuePattern = source.valuePattern
            }
            QValue build() {
                return new QValue(this)
            }
        }

    }
}
