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

    private static final Logger logger =
            LoggerFactory.getLogger(MetadataImpl.class.getName())

    private final Map<String, MetadataAttribute> attributes

    protected MetadataImpl(Map<String, MetadataAttribute> attributes) {
        this.attributes = attributes
    }
    /*
    protected MetadataImpl(Map<String, String> map) {
        for (String key : map.keySet) {
            MetadataAttribute attribute =
                    new MetadataAttribute(key, map.get(key).getValue)
            attributes.put(key, attribute)
        }
    }
    */
    // ------------ implements Metadata -------------------
    @Override
    boolean containsKey(String key) {
        return attributes.containsKey(key)
    }

    @Override
    String get(String key) {
        MetadataAttribute attribute = attributes.get(key)
        if (attribute != null) {
            return attribute.getValue()
        } else {
            return null
        }
    }

    @Override
    MetadataAttribute getMetadataAttribute(String key) {
        return attributes.get(key)
    }

    @Override
    boolean isEmpty() {
        return attributes.isEmpty()
    }

    @Override
    Set<String> keySet() {
        return attributes.keySet()
    }

    @Override
    int size()  {
        attributes.size()
    }


    // -------------- overrides methods of Metadata -------------------

    @Override
    String toURLAsString() {
        if (attributes.containsKey(KEY_URL_FRAGMENT)) {
            return toURL().toExternalForm() +
                    "#" + attributes.get(KEY_URL_FRAGMENT).getValue()
        } else {
            return toURL().toExternalForm()
        }
    }

    @Override
    URL toURL() {
        if (attributes.containsKey(KEY_URL_PROTOCOL) && attributes.containsKey(KEY_URL_HOST)) {
            StringBuilder sb = new StringBuilder()
            sb.append(attributes.get(KEY_URL_PROTOCOL).getValue())
            sb.append(":")
            if (attributes.get(KEY_URL_PROTOCOL).getValue().startsWith("http")) {
                sb.append("//")
            }
            sb.append(attributes.get(KEY_URL_HOST).getValue())
            if (attributes.containsKey(KEY_URL_PORT) &&
                    attributes.get(KEY_URL_PORT).getValue() != "80") {
                sb.append(":")
                sb.append(attributes.get(KEY_URL_PORT).getValue())
            }
            sb.append(attributes.get(KEY_URL_PATH).getValue())
            if (attributes.containsKey(KEY_URL_QUERY)) {
                sb.append("?")
                sb.append(attributes.get(KEY_URL_QUERY).getValue())
            }
            return new URL(sb.toString())
        } else {
            return null
        }
    }

    @Override
    void annotate(QueryOnMetadata query) {
        Objects.requireNonNull(query)
        attributes.keySet().forEach { key ->
            if (matchesByAster(query, key)) {
                attributes.get(key).setMatchedByAster(true)
            }
            if (matchesIndividually(query, key)) {
                attributes.get(key).setMatchedIndividually(true)
            }
        }
    }

    @Override
    void toSpanSequence(MarkupBuilder mb, QueryOnMetadata query) {
        Objects.requireNonNull(mb)
        Objects.requireNonNull(query)
        int count = 0
        List<String> keys = new ArrayList<String>(attributes.keySet())
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

    private boolean matchesByAster(QueryOnMetadata query, String key) {
        return query.containsKey("*") &&
                query.get("*").matches(this.get(key))
    }

    private boolean matchesIndividually(QueryOnMetadata query, String key) {
        return query.containsKey(key) &&
                this.containsKey(key) &&
                query.get(key).matches(this.get(key))
    }

    private String getCSSClassNameSolo(QueryOnMetadata query, String key) {
        if (matchesByAster(query, key) || matchesIndividually(query, key)) {
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
        List<String> keys = new ArrayList<String>(attributes.keySet())
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

    @Override
    void annotate(QueryOnMetadata leftQuery,
                  QueryOnMetadata rightQuery,
                  IgnoreMetadataKeys ignoreMetadataKeys,
                  IdentifyMetadataValues identifyMetadataValues) {
        Objects.requireNonNull(leftQuery)
        Objects.requireNonNull(rightQuery)
        Objects.requireNonNull(ignoreMetadataKeys)
        Objects.requireNonNull(identifyMetadataValues)
        Set<String> keys = attributes.keySet()
        keys.forEach { key ->

            MetadataAttribute attribute = attributes.get(key)

            if (ignoreMetadataKeys.contains(key)) {
                attribute.setIgnoredByKey(true)
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
        }
    }

    private String getCSSClassName(QueryOnMetadata left,
                                   QueryOnMetadata right,
                                   String key,
                                   IdentifyMetadataValues identifyMetadataValues) {
        boolean canBePaired = this.canBePaired(left, right, key)
        boolean canBeIdentified = this.canBeIdentified(key, identifyMetadataValues)
        if (canBePaired) {
            return "matched-value"
        } else if (canBeIdentified) {
            return "identified-value"
        } else {
            return null
        }
    }

    private boolean canBePaired(QueryOnMetadata left,
                                QueryOnMetadata right,
                                String key) {
        return left.containsKey("*")  && left.get("*").matches(this.get(key)) ||
                left.containsKey(key)      && left.get(key).matches(this.get(key)) ||
                right.containsKey("*") && right.get("*").matches(this.get(key)) ||
                right.containsKey(key)     && right.get(key).matches(this.get(key))
    }

    private boolean canBeIdentified(
            String key,
            IdentifyMetadataValues identifyMetadataValues) {
        return identifyMetadataValues.containsKey(key) &&
                identifyMetadataValues.matches(this)
    }


    //--------JSONifiable----------------------------------------------
    @Override
    String toJson() {
        //return new JsonOutput().toJson(metadata)
        StringBuilder sb = new StringBuilder()
        int entryCount = 0
        sb.append("{")
        assert attributes != null, "metadata_ is null before iterating over keys"
        //println "keys: ${metadata_.keySet()}"
        List<String> keys = new ArrayList<String>(attributes.keySet())
        Map<String, MetadataAttribute> copy = new HashMap<>(attributes)
        // sort by the key
        Collections.sort(keys)
        keys.each { key ->
            if (entryCount > 0) {
                sb.append(", ")    // comma followed by a white space
            }
            sb.append('"' + JsonUtil.escapeAsJsonString(key) + '"')
            sb.append(':')
            sb.append('"' + copy.get(key).getValue() + '"')
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
