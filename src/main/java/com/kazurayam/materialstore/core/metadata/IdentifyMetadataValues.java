package com.kazurayam.materialstore.core.metadata;

import com.kazurayam.materialstore.core.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public abstract class IdentifyMetadataValues {

    private static final Logger logger = LoggerFactory.getLogger(IdentifyMetadataValues.class);

    public static final IdentifyMetadataValues NULL_OBJECT = new Builder().build();

    public abstract int size();

    public abstract boolean containsKey(String key);

    public abstract Set<String> keySet();

    public abstract Pattern getPattern(String key);

    public abstract boolean matches(Metadata metadata);


    /**
     *
     */
    public static class Builder {

        private Map<String, Pattern> attributeNameRegexPair;

        public Builder() {
            this.attributeNameRegexPair = new HashMap<>();
        }

        public Builder putNameRegexPair(String attributeName, String regex) {
            Objects.requireNonNull(attributeName);
            Objects.requireNonNull(regex);
            try {
                Pattern pattern = Pattern.compile(regex);
                attributeNameRegexPair.put(attributeName, pattern);
            } catch (PatternSyntaxException e) {
                logger.warn("attributeName=" + attributeName + ",regex=" + regex + "\n" + e.getMessage());
            }
            return this;
        }

        public Builder putAllNameRegexPairs(Map<String, String> pairs) {
            Objects.requireNonNull(pairs);
            for (Map.Entry<String, String> entry : pairs.entrySet()) {
                putNameRegexPair(entry.getKey(), entry.getValue());
            }
            return this;
        }

        public IdentifyMetadataValues build() {
            return new IdentifyMetadataValuesImpl(attributeNameRegexPair);
        }

        public Map<String, Pattern> getAttributeNameRegexPair() {
            return attributeNameRegexPair;
        }

        public void setAttributeNameRegexPair(Map<String, Pattern> attributeNameRegexPair) {
            this.attributeNameRegexPair = attributeNameRegexPair;
        }

    }
}
