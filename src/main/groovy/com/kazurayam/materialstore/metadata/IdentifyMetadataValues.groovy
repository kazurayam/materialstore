package com.kazurayam.materialstore.metadata


import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

abstract class IdentifyMetadataValues {

    private static final Logger logger = LoggerFactory.getLogger(IdentifyMetadataValues.class)

    public static final IdentifyMetadataValues NULL_OBJECT = new Builder().build()

    abstract int size()

    abstract boolean containsKey(String key)

    abstract Set<String> keySet()

    abstract Pattern get(String key)

    abstract boolean matches(Metadata metadata)


    /**
     *
     */
    static class Builder {
        Map<String, Pattern > attributeNameRegexPair
        Builder() {
            this.attributeNameRegexPair = new HashMap<>();
        }
        Builder put(String attributeName, String regex) {
            Objects.requireNonNull(attributeName)
            Objects.requireNonNull(regex)
            try {
                Pattern pattern = Pattern.compile(regex)
                attributeNameRegexPair.put(attributeName, pattern)
            } catch (PatternSyntaxException e) {
                logger.warn("attributeName=" + attributeName +
                        ",regex=" + regex +
                        "\n" + e.getMessage())
            }
            return this
        }
        Builder putAll(Map<String, String> pairs) {
            Objects.requireNonNull(pairs)
            pairs.forEach({ key, value ->
                this.put(key, value)
            })
            return this
        }
        IdentifyMetadataValues build() {
            return new IdentifyMetadataValuesImpl(attributeNameRegexPair)
        }
    }
}