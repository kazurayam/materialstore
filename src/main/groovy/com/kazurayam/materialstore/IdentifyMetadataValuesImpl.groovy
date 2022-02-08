package com.kazurayam.materialstore

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.regex.Matcher
import java.util.regex.Pattern

class IdentifyMetadataValuesImpl extends IdentifyMetadataValues {

    private static final Logger logger = LoggerFactory.getLogger(IdentifyMetadataValuesImpl.class)

    private Map<String, Pattern> attributeNameRegexPairs

    IdentifyMetadataValuesImpl(Map<String, Pattern> attributeNameRegexPairs) {
        this.attributeNameRegexPairs = attributeNameRegexPairs
    }

    @Override
    int size() {
        return attributeNameRegexPairs.size()
    }

    @Override
    boolean containsKey(String key) {
        return attributeNameRegexPairs.containsKey(key)
    }

    @Override
    Set<String> keySet() {
        return attributeNameRegexPairs.keySet()
    }

    @Override
    Pattern get(String key) {
        return attributeNameRegexPairs.get(key)
    }

    @Override
    boolean matches(String key, String value) {
        Objects.requireNonNull(key)
        Objects.requireNonNull(value)
        Pattern pattern = get(key)
        if (pattern != null) {
            Matcher m = pattern.matcher(value)
            boolean b = m.matches()
            if (!b) {
                logger.info("key=\"${key}\", value=\"${value}\" does not match with regex \"${pattern.toString()}\"")
            }
            return b
        } else {
            return false
        }
    }
}
