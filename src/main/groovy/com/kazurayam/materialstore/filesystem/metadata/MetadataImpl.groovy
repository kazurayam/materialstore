package com.kazurayam.materialstore.filesystem.metadata

import com.google.gson.Gson
import com.kazurayam.materialstore.filesystem.Metadata
import com.kazurayam.materialstore.filesystem.QueryOnMetadata
import com.kazurayam.materialstore.util.GsonHelper
import com.kazurayam.materialstore.util.JsonUtil
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
    boolean matchesByAster(QueryOnMetadata query, String key) {
        return query.containsKey("*") &&
                query.get("*").matches(this.get(key))
    }

    @Override
    boolean matchesIndividually(QueryOnMetadata query, String key) {
        return query.containsKey(key) &&
                this.containsKey(key) &&
                query.get(key).matches(this.get(key))
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
            if (canBePaired(leftQuery, rightQuery, key)) {
                attribute.setPaired(true)
            }
            if (canBeIdentified(key, identifyMetadataValues)) {
                attribute.setIdentifiedByValue(true)
            }
            //
            Matcher m = SemanticVersionAwareStringMatcher.straightMatcher(this.get(key))
            if (m.matches()) {
                attribute.setSemanticVersion(m.group(2))
            }
        }
    }

    @Override
    boolean canBeIdentified(String key, IdentifyMetadataValues identifyMetadataValues) {
        return identifyMetadataValues.containsKey(key) &&
                identifyMetadataValues.matches(this)
    }

    @Override
    boolean canBePaired(QueryOnMetadata left, QueryOnMetadata right, String key) {
        return left.containsKey("*")  && left.get("*").matches(this.get(key)) ||
                left.containsKey(key)      && left.get(key).matches(this.get(key)) ||
                right.containsKey("*") && right.get("*").matches(this.get(key)) ||
                right.containsKey(key)     && right.get(key).matches(this.get(key))
    }

    @Override
    String toSimplifiedJson() {
        StringBuilder sb = new StringBuilder()
        int entryCount = 0;
        sb.append("{")
        List<String> keys = getSortedKeys(attributes)
        keys.each { key ->
            if (entryCount > 0) {
                sb.append(", ")    // comma followed by a white space
            }
            sb.append('"' + JsonUtil.escapeAsJsonString(key) + '"')
            sb.append(':')
            MetadataAttribute attribute = attributes.get(key)
            sb.append('"' + attribute.getValue() + '"')
            entryCount += 1
        }
        sb.append("}")
        return sb.toString()
    }

    private static List<String> getSortedKeys(Map<String, MetadataAttribute> attributes) {
        List<String> keys = new ArrayList<String>(attributes.keySet())
        Collections.sort(keys)
        return keys
    }

    //--------Jsonifiable----------------------------------------------
    @Override
    String toJson() {
        //return new JsonOutput().toJson(metadata)
        StringBuilder sb = new StringBuilder()
        int entryCount = 0
        sb.append("{")
        List<String> keys = getSortedKeys(attributes)
        keys.each { key ->
            if (entryCount > 0) {
                sb.append(", ")    // comma followed by a white space
            }
            sb.append('"' + JsonUtil.escapeAsJsonString(key) + '"')
            sb.append(':')
            sb.append(attributes.get(key).toJson())
            entryCount += 1
        }
        sb.append("}")
        // WARNING: should not pretty-print this. it will causes a lot of problems
        return sb.toString()
    }

    @Override
    String toJson(boolean prettyPrint) {
        if (prettyPrint) {
            return JsonUtil.prettyPrint(toJson())
        } else {
            return toJson()
        }
    }

    //--------TemplateReady--------------------------------------------
    @Override
    Map<String, Object> toTemplateModel() {
        // convert JSON string to Java Map
        Map<String, Object> map = new Gson().fromJson(toJson(), Map.class)
        return map
    }

    @Override
    String toTemplateModelAsJson() {
        return toTemplateModelAsJson(false)
    }

    @Override
    String toTemplateModelAsJson(boolean prettyPrint) {
        Gson gson = GsonHelper.createGson(prettyPrint)
        Map<String, Object> model = toTemplateModel()
        return gson.toJson(model)
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
