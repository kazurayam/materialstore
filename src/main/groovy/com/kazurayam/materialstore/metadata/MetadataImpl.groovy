package com.kazurayam.materialstore.metadata

import com.kazurayam.materialstore.util.JsonUtil
import groovy.xml.MarkupBuilder

import java.util.regex.Matcher

/**
 *
 */
final class MetadataImpl extends Metadata {

    private final Map<String, String> metadata

    protected MetadataImpl(Map<String, String> metadata) {
        this.metadata = metadata
    }


    // ------------ implements Metadata -------------------
    @Override
    boolean containsKey(String key) {
        return metadata.containsKey((String)key)
    }

    @Override
    String get(String key) {
        return metadata.get(key)
    }

    @Override
    boolean isEmpty() {
        return metadata.isEmpty()
    }

    @Override
    Set<String> keySet() {
        return metadata.keySet()
    }



    @Override
    int size()  {
        metadata.size()
    }


    // -------------- overrides methods of Metadata -------------------

    @Override
    URL toURL() {
        if (metadata.containsKey(KEY_URL_HOST)) {
            StringBuilder sb = new StringBuilder()
            sb.append(metadata.get(KEY_URL_PROTOCOL))
            sb.append("://")
            sb.append(metadata.get(KEY_URL_HOST))
            sb.append(metadata.get(KEY_URL_PATH))
            if (metadata.containsKey(KEY_URL_QUERY)) {
                sb.append("?")
                sb.append(metadata.get(KEY_URL_QUERY))
            }
            return new URL(sb.toString())
        } else {
            return null
        }
    }

    @Override
    void toSpanSequence(MarkupBuilder mb, MetadataPattern metadataPattern) {
        Objects.requireNonNull(mb)
        Objects.requireNonNull(metadataPattern)
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
            String cssClassName = getCSSClassNameSolo(metadataPattern, key)
            if (cssClassName != null) {
                mb.span(class: "matched-value",
                        "\"${JsonUtil.escapeAsJsonString(this.get(key))}\"")
            } else {
                mb.span("\"${JsonUtil.escapeAsJsonString(this.get(key))}\"")
            }
            count += 1
        }
        mb.span("}")
    }

    private String getCSSClassNameSolo(MetadataPattern metadataPattern, String key) {
        boolean matchesByAster = metadataPattern.containsKey("*") &&
                metadataPattern.get("*").matches(this.get(key))

        boolean matchesIndividually = metadataPattern.containsKey(key) &&
                this.containsKey(key) &&
                metadataPattern.get(key).matches(this.get(key))

        if (matchesByAster || matchesIndividually) {
            return "matched-value"
        } else {
            return null
        }

    }


    @Override
    void toSpanSequence(MarkupBuilder mb,
                        MetadataPattern leftMetadataPattern,
                        MetadataPattern rightMetadataPattern,
                        IgnoreMetadataKeys ignoreMetadataKeys,
                        IdentifyMetadataValues identifyMetadataValues) {
        Objects.requireNonNull(mb)
        Objects.requireNonNull(leftMetadataPattern)
        Objects.requireNonNull(rightMetadataPattern)
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
            String cssClass = getCSSClassName(
                    leftMetadataPattern, rightMetadataPattern,
                    key,
                    identifyMetadataValues)
            if (cssClass != null) {
                mb.span(class: cssClass,
                        "\"${JsonUtil.escapeAsJsonString(this.get(key))}\"")
            } else {
                Matcher m = SemanticVersionAwareStringMatcher.straightMatcher(this.get(key))
                if (m.matches()) {
                    // <span>"/npm/bootstrap-icons@</span><span class='semantic-version'>1.5.0</span><span>/font/bootstrap-icons.css"</span>
                    mb.span("\"${JsonUtil.escapeAsJsonString(m.group(1))}")
                    mb.span(class: "semantic-version",
                            "${JsonUtil.escapeAsJsonString(m.group(2))}")
                    mb.span("${JsonUtil.escapeAsJsonString(m.group(4))}\"")
                } else {
                    // <span>xxxxxxx</span>
                    mb.span("\"${JsonUtil.escapeAsJsonString(this.get(key))}\"")
                }
            }
            //
            count += 1
        }
        mb.span("}")
    }


    private String getCSSClassName(MetadataPattern left, MetadataPattern right,
                                   String key,
                                   IdentifyMetadataValues identifyMetadataValues) {
        boolean canBePaired = (
                left.containsKey("*")  &&
                        left.get("*").matches(this.get(key)) ||
                left.containsKey(key)      &&
                        left.get(key).matches(this.get(key)) ||
                right.containsKey("*") &&
                        right.get("*").matches(this.get(key)) ||
                right.containsKey(key)     &&
                        right.get(key).matches(this.get(key))        )

        boolean canBeIdentified = (
                identifyMetadataValues.containsKey(key) &&
                identifyMetadataValues.matches(this)        )

        if (canBePaired) {
            return "matched-value"
        } else if (canBeIdentified) {
            return "identified-value"
        } else {
            return null
        }
    }

    // ------- overriding java.lang.Object -------
    @Override
    String toString() {
        //return new JsonOutput().toJson(metadata)
        StringBuilder sb = new StringBuilder()
        int entryCount = 0
        sb.append("{")
        assert metadata != null, "metadata_ is null before iterating over keys"
        //println "keys: ${metadata_.keySet()}"
        List<String> keys = new ArrayList<String>(metadata.keySet())
        Map<String,String> copy = new HashMap<String,String>(metadata)
        // sort by the key
        Collections.sort(keys)
        keys.each { key ->
            if (entryCount > 0) {
                sb.append(", ")    // comma followed by a white space
            }
            sb.append('"')
            assert copy != null, "metadata_ is null for key=${key}"
            sb.append(JsonUtil.escapeAsJsonString(key))
            sb.append('"')
            sb.append(':')
            sb.append('"')
            sb.append(JsonUtil.escapeAsJsonString(copy.get(key)))
            sb.append('"')
            entryCount += 1
        }
        sb.append("}")
        return sb.toString()
    }

    @Override
    boolean equals(Object obj) {
        if (! obj instanceof MetadataImpl) {
            return false
        }
        MetadataImpl other = (MetadataImpl)obj
        if (other.size() != other.metadata.size()) {
            return false
        }
        //
        Set otherKeySet = other.keySet()
        if (this.keySet() != otherKeySet) {
            return false
        }
        //
        boolean result = true
        this.keySet().each { key ->
            if (this.get(key) != other.get(key)) {
                result = false
                return
            }
        }
        return result
    }


    @Override
    int hashCode() {
        int hash = 7
        this.keySet().each { key ->
            hash = 31 * hash + key.hashCode()
            hash = 31 * hash + this.get(key).hashCode()
        }
        return hash
    }



    // ------------ comparable ----------------
    @Override
    int compareTo(Object obj) {
        if (! obj instanceof MetadataImpl) {
            throw new IllegalArgumentException("obj is not instance of Metadata")
        }
        MetadataImpl other = (MetadataImpl)(obj)
        return this.toString() <=> other.toString()
    }

}
