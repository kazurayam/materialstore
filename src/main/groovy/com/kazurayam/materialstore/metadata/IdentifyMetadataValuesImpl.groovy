package com.kazurayam.materialstore.metadata


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
    boolean containsKey(String key) {
        return attributeNameRegexPairs.containsKey(key)
    }

    @Override
    Pattern get(String key) {
        return attributeNameRegexPairs.get(key)
    }

    @Override
    Set<String> keySet() {
        return attributeNameRegexPairs.keySet()
    }

    @Override
    boolean matches(Metadata metadata) {
        Objects.requireNonNull(metadata)
        for (String key in keySet()) {
            if (metadata.containsKey(key)) {
                String value = metadata.get(key)
                boolean attributeMatched = matchesWithAttributeOf(key, value)
                if (attributeMatched) {
                    return true
                }
            } // else next key
        }
        return false
    }

    boolean matchesWithAttributeOf(String key, String value) {
        Objects.requireNonNull(key)
        Objects.requireNonNull(value)
        Pattern pattern = get(key)
        if (pattern != null) {
            Matcher m = pattern.matcher(value)
            boolean b = m.matches()
            if (!b) {
                logger.debug("key=\"${key}\", value=\"${value}\" does not match with regex \"${pattern.toString()}\"")
            }
            return b
        } else {
            return false
        }
    }

    @Override
    int size() {
        return attributeNameRegexPairs.size()
    }

}
