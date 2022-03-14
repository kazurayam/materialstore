package com.kazurayam.materialstore.filesystem.metadata

import com.google.gson.Gson
import com.kazurayam.materialstore.filesystem.Metadata
import com.kazurayam.materialstore.filesystem.QueryOnMetadata
import com.kazurayam.materialstore.util.JsonUtil
import groovy.xml.MarkupBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.regex.Matcher

/**
 *
 */
final class MetadataImpl extends Metadata {

    private static final Logger logger = LoggerFactory.getLogger(MetadataImpl.class.getName())

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
    String toURLAsString() {
        if (metadata.containsKey(KEY_URL_FRAGMENT)) {
            return this.toURL().toExternalForm() + "#" + metadata.get(KEY_URL_FRAGMENT)
        } else {
            return this.toURL().toExternalForm()
        }
    }

    @Override
    URL toURL() {
        if (metadata.containsKey(KEY_URL_PROTOCOL) && metadata.containsKey(KEY_URL_HOST)) {
            StringBuilder sb = new StringBuilder()
            sb.append(metadata.get(KEY_URL_PROTOCOL))
            sb.append(":")
            if (metadata.get(KEY_URL_PROTOCOL).startsWith("http")) {
                sb.append("//")
            }
            sb.append(metadata.get(KEY_URL_HOST))
            if (metadata.containsKey(KEY_URL_PORT) && metadata.get(KEY_URL_PORT) != "80") {
                sb.append(":")
                sb.append(metadata.get(KEY_URL_PORT))
            }
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
            String cssClassName = getCSSClassNameSolo(query, key)
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

    private String getCSSClassNameSolo(QueryOnMetadata query, String key) {
        boolean matchesByAster = query.containsKey("*") &&
                query.get("*").matches(this.get(key))

        boolean matchesIndividually = query.containsKey(key) &&
                this.containsKey(key) &&
                query.get(key).matches(this.get(key))

        if (matchesByAster || matchesIndividually) {
            return "matched-value"
        } else {
            return null
        }

    }


    @Override
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
            String cssClass = getCSSClassName(
                    leftQuery, rightQuery,
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


    private String getCSSClassName(QueryOnMetadata left, QueryOnMetadata right,
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

    //--------JSONifiable----------------------------------------------
    @Override
    String toJson() {
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
        // WARNING: should not pretty-print this. it will causes a lot of problems
        return sb.toString()
    }

    //--------TemplateReady--------------------------------------------
    @Override
    Map<String, Object> toTemplateModel() {
        // convert JSON string to Java Map
        Map<String, Object> map = new Gson().fromJson(toJson(), Map.class)
        return map
    }

    // ------- overriding java.lang.Object -------
    @Override
    String toString() {
        return toJson()
    }


    @Override
    boolean equals(Object obj) {
        if (! obj instanceof MetadataImpl) {
            return false
        }
        MetadataImpl other = (MetadataImpl)obj
        /*
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
         */
        return this.toString() == other.toString()
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
