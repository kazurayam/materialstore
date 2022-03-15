package com.kazurayam.materialstore.report.markupbuilder_templates

import com.kazurayam.materialstore.filesystem.Metadata
import com.kazurayam.materialstore.filesystem.QueryOnMetadata
import com.kazurayam.materialstore.filesystem.metadata.IdentifyMetadataValues
import com.kazurayam.materialstore.filesystem.metadata.IgnoreMetadataKeys
import com.kazurayam.materialstore.filesystem.metadata.SemanticVersionAwareStringMatcher
import com.kazurayam.materialstore.util.JsonUtil
import groovy.xml.MarkupBuilder

import java.util.regex.Matcher

class MetadataTemplate {

    private final Metadata metadata

    MetadataTemplate(Metadata metadata) {
        this.metadata = metadata
    }

    boolean canBeIdentified(String key, IdentifyMetadataValues identifyMetadataValues) {
        return identifyMetadataValues.containsKey(key) &&
                identifyMetadataValues.matches(metadata)
    }

    boolean canBePaired(QueryOnMetadata left, QueryOnMetadata right, String key) {
        return left.containsKey("*")  && left.get("*").matches(metadata.get(key)) ||
                left.containsKey(key)      && left.get(key).matches(metadata.get(key)) ||
                right.containsKey("*") && right.get("*").matches(metadata.get(key)) ||
                right.containsKey(key)     && right.get(key).matches(metadata.get(key))
    }

    String getCSSClassName(QueryOnMetadata left,
                           QueryOnMetadata right,
                           String key,
                           IdentifyMetadataValues identifyMetadataValues) {
        boolean canBePaired = new MetadataTemplate(metadata).canBePaired(left, right, key)
        boolean canBeIdentified = new MetadataTemplate(metadata).canBeIdentified(key, identifyMetadataValues)
        if (canBePaired) {
            return "matched-value"
        } else if (canBeIdentified) {
            return "identified-value"
        } else {
            return null
        }
    }

    String getCSSClassNameSolo(QueryOnMetadata query, String key) {
        if (matchesByAster(query, key) || matchesIndividually(query, key)) {
            return "matched-value"
        } else {
            return null
        }
    }

    boolean matchesByAster(QueryOnMetadata query, String key) {
        return query.containsKey("*") &&
                query.get("*").matches(metadata.get(key))
    }

    boolean matchesIndividually(QueryOnMetadata query, String key) {
        return query.containsKey(key) &&
                metadata.containsKey(key) &&
                query.get(key).matches(metadata.get(key))
    }

    void toSpanSequence(MarkupBuilder mb, QueryOnMetadata query) {
        Objects.requireNonNull(mb)
        Objects.requireNonNull(query)
        int count = 0
        List<String> keys = new ArrayList<String>(metadata.keySet())
        Collections.sort(keys)
        mb.span("{")
        keys.forEach {key ->
            if (count > 0) {
                mb.span(", ")
            }
            // make the <span> of the "key" part of an attribute of Metadata
            mb.span("\"${JsonUtil.escapeAsJsonString(key)}\"" + ":")

            // make the <span> of the "value" part of an attribute of Metadata
            String cssClassName = new MetadataTemplate(metadata).getCSSClassNameSolo(query, key)
            if (cssClassName != null) {
                mb.span(class: "matched-value",
                        "\"${JsonUtil.escapeAsJsonString(metadata.get(key))}\"")
            } else {
                mb.span("\"${JsonUtil.escapeAsJsonString(metadata.get(key))}\"")
            }
            count += 1
        }
        mb.span("}")
    }

    void toSpanSequence(MarkupBuilder mb,
                               QueryOnMetadata leftQuery,
                               QueryOnMetadata rightQuery,
                               IgnoreMetadataKeys ignoreMetadataKeys,
                               IdentifyMetadataValues identifyMetadataValues) {
        Objects.requireNonNull(mb)
        Objects.requireNonNull(leftQuery)
        Objects.requireNonNull(rightQuery)
        Objects.requireNonNull(ignoreMetadataKeys)
        Objects.requireNonNull(identifyMetadataValues)
        int count = 0
        List<String> keys = new ArrayList<String>(metadata.keySet())
        Collections.sort(keys)
        mb.span("{")
        keys.forEach { key ->
            if (count > 0) {
                mb.span(", ")
            }

            // make the <span> of the "key" part of an attribute of Metadata
            if (ignoreMetadataKeys.contains(key)) {
                mb.span(class: "ignored-key", "\"${JsonUtil.escapeAsJsonString(key)}\"" + ":")
            } else {
                mb.span("\"${JsonUtil.escapeAsJsonString(key)}\"" + ":")
            }

            // make the <span> of the "value" part of an attribute of Metadata
            String cssClass = new MetadataTemplate(metadata).getCSSClassName(leftQuery, rightQuery,
                    key, identifyMetadataValues)
            if (cssClass != null) {
                mb.span(class: cssClass,
                        "\"${JsonUtil.escapeAsJsonString(metadata.get(key))}\"")
            } else {
                Matcher m = SemanticVersionAwareStringMatcher.straightMatcher(metadata.get(key))
                if (m.matches()) {
                    // <span>"/npm/bootstrap-icons@</span><span class='semantic-version'>1.5.0</span><span>/font/bootstrap-icons.css"</span>
                    mb.span("\"${JsonUtil.escapeAsJsonString(m.group(1))}")
                    mb.span(class: "semantic-version",
                            "${JsonUtil.escapeAsJsonString(m.group(2))}")
                    mb.span("${JsonUtil.escapeAsJsonString(m.group(4))}\"")
                } else {
                    // <span>xxxxxxx</span>
                    mb.span("\"${JsonUtil.escapeAsJsonString(metadata.get(key))}\"")
                }
            }
            //
            count += 1
        }
        mb.span("}")
    }
}
