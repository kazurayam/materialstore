package com.kazurayam.materialstore.core.metadata;

import com.kazurayam.materialstore.core.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class IdentifyMetadataValuesImpl extends IdentifyMetadataValues {

    private static final Logger logger = LoggerFactory.getLogger(IdentifyMetadataValuesImpl.class);

    private final Map<String, Pattern> attributeNameRegexPairs;

    public IdentifyMetadataValuesImpl(Map<String, Pattern> attributeNameRegexPairs) {
        this.attributeNameRegexPairs = attributeNameRegexPairs;
    }

    @Override
    public boolean containsKey(String key) {
        return attributeNameRegexPairs.containsKey(key);
    }

    @Override
    public Pattern getPattern(String key) {
        return attributeNameRegexPairs.get(key);
    }

    @Override
    public Set<String> keySet() {
        return attributeNameRegexPairs.keySet();
    }

    @Override
    public boolean matches(Metadata metadata) {
        Objects.requireNonNull(metadata);
        for (String key : keySet()) {
            if (metadata.containsKey(key)) {
                String value = metadata.get(key);
                boolean attributeMatched = matchesWithAttributeOf(key, value);
                if (attributeMatched) {
                    return true;
                }

            }
            // else next key
        }

        return false;
    }

    public boolean matchesWithAttributeOf(final String key, final String value) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        final Pattern pattern = getPattern(key);
        if (pattern != null) {
            Matcher m = pattern.matcher(value);
            boolean b = m.matches();
            if (!b) {
                logger.debug("key=\"" + key + "\", value=\"" + value + "\" does not match with regex \"" + pattern.toString() + "\"");
            }
            return b;
        } else {
            return false;
        }

    }

    @Override
    public int size() {
        return attributeNameRegexPairs.size();
    }

}
