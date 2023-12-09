package com.kazurayam.materialstore.core.metadata;

import com.kazurayam.materialstore.core.MaterialLocator;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.Metadata;
import com.kazurayam.materialstore.core.QueryOnMetadata;
import com.kazurayam.materialstore.core.SortKeys;
import com.kazurayam.materialstore.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;

/**
 *
 */
public final class MetadataImpl extends Metadata {

    private static final Logger logger = LoggerFactory.getLogger(MetadataImpl.class.getName());
    private final Map<String, MetadataAttribute> attributes;
    private URL url;

    public MetadataImpl(Map<String, MetadataAttribute> attributes) {
        this.attributes = attributes;
        this.url = null;
    }


    @Override
    public void annotate(final QueryOnMetadata query) {
        Objects.requireNonNull(query);
        attributes.keySet().forEach( key -> {
            if (matchesByAster(query, key)) {
                attributes.get(key).setMatchedByAster(true);
            }
            if (matchesIndividually(query, key)) {
                attributes.get(key).setMatchedIndividually(true);
            }
        });
    }

    @Override
    public void annotate(final QueryOnMetadata query,
                         final IgnoreMetadataKeys ignoreMetadataKeys,
                         final IdentifyMetadataValues identifyMetadataValues) {
        Objects.requireNonNull(query);
        Objects.requireNonNull(ignoreMetadataKeys);
        Objects.requireNonNull(identifyMetadataValues);
        Set<String> keys = attributes.keySet();
        keys.forEach( key -> {
            MetadataAttribute attribute = attributes.get(key);
            if (ignoreMetadataKeys.contains(key)) {
                attribute.setIgnoredByKey(true);
            }
            if (canBePaired(query, key)) {
                attribute.setPaired(true);
            }
            if (canBeIdentified(key, identifyMetadataValues)) {
                attribute.setIdentifiedByValue(true);
            }
            //
            Matcher m = SemanticVersionPattern.straightMatcher(this.get(key));
            if (m.matches()) {
                SemanticVersionMatcherResult result = new SemanticVersionMatcherResult(m);
                attribute.setSemanticVersionMatcherResult(result);
            }
        });
    }

    @Override
    public boolean canBeIdentified(String key, IdentifyMetadataValues identifyMetadataValues) {
        return identifyMetadataValues.containsKey(key) && identifyMetadataValues.matches(this);
    }

    @Override
    public boolean canBePaired(QueryOnMetadata query, String key) {
        return query.containsKey("*") && query.get("*").matches(this.get(key)) || query.containsKey(key) && query.get(key).matches(this.get(key));
    }

    @Override
    public int compareTo(Metadata obj) {
        MetadataImpl other = (MetadataImpl) (obj);
        return this.toString().compareTo(other.toString());
    }

    @Override
    public boolean containsCategoryDiff() {
        if (this.containsKey("category")) {
            return (this.get("category").equals("diff"));
        } else {
            return false;
        }
    }
    @Override
    public boolean containsKey(String key) {
        return attributes.containsKey(key);
    }

    /*
     * compares this.toSimplifiedJson() with other.toSimplifiedJson()
     */
    @Override
    public boolean equals(Object obj) {
        if ( !(obj instanceof MetadataImpl) ) {
            return false;
        }
        MetadataImpl other = (MetadataImpl) obj;
        return this.toSimplifiedJson().equals(other.toSimplifiedJson());
    }



    @Override
    public String get(String key) {
        MetadataAttribute attribute = attributes.get(key);
        if (attribute != null) {
            return attribute.getValue();
        } else {
            return null;
        }
    }

    @Override
    public MaterialLocator getMaterialLocatorLeft() {
        if (this.containsCategoryDiff()) {
            if (this.containsKey("left")) {
                return MaterialLocator.parse(this.get("left"));
            } else {
                return MaterialLocator.NULL_OBJECT;
            }
        } else {
            return MaterialLocator.NULL_OBJECT;
        }
    }

    @Override
    public MaterialLocator getMaterialLocatorRight() {
        if (this.containsCategoryDiff()) {
            if (this.containsKey("right")) {
                return MaterialLocator.parse(this.get("right"));
            } else {
                return MaterialLocator.NULL_OBJECT;
            }
        } else {
            return MaterialLocator.NULL_OBJECT;
        }
    }

    @Override
    public MetadataAttribute getMetadataAttribute(String key) {
        return attributes.get(key);
    }

    @Override
    public MetadataIdentification getMetadataIdentification() {
        return this.getMetadataIdentification(new SortKeys());
    }

    @Override
    public MetadataIdentification getMetadataIdentification(SortKeys sortKeys) {
        String simplifiedJson = this.toSimplifiedJson(sortKeys);
        return new MetadataIdentification(simplifiedJson);
    }

    static List<String> getSortedKeys(
            Map<String, MetadataAttribute> attributes,
            SortKeys nominated) {
        List<String> target = new ArrayList<>();
        List<String> source = new ArrayList<>(attributes.keySet());
        Collections.sort(source, String.CASE_INSENSITIVE_ORDER);
        Iterator<String> nominatedIter = nominated.iterator();
        while (nominatedIter.hasNext()) {
            String n = nominatedIter.next();
            Iterator<String> sourceIter = source.iterator();
            while (sourceIter.hasNext()) {
                String e = sourceIter.next();
                if (n.equals(e)) {
                    target.add(n);
                    sourceIter.remove();
                }
            }
        }
        target.addAll(source);
        return target;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        for (String key : this.keySet()) {
            hash = 31 * hash + key.hashCode();
            hash = 31 * hash + Objects.requireNonNull(this.get(key)).hashCode();
        }
        return hash;
    }


    @Override
    public boolean isEmpty() {
        return attributes.isEmpty();
    }

    @Override
    public Set<String> keySet() {
        return attributes.keySet();
    }

    @Override
    public boolean matchesByAster(QueryOnMetadata query, String key) {
        return query.containsKey("*") && query.get("*").matches(this.get(key));
    }

    @Override
    public boolean matchesIndividually(QueryOnMetadata query, String key) {
        return query.containsKey(key) && this.containsKey(key) && query.get(key).matches(this.get(key));
    }

    public void setURL(URL url) {
        this.url = url;
    }

    @Override
    public int size() {
        return attributes.size();
    }

    @Override
    public String toJson() {
        final StringBuilder sb = new StringBuilder();
        int entryCount = 0;
        sb.append("{");
        List<String> keys = getSortedKeys(attributes, new SortKeys());
        for (String key : keys) {
            if (entryCount > 0) {
                sb.append(", ");// comma followed by a white space
            }
            sb.append("\"");
            sb.append(JsonUtil.escapeAsJsonString(key));
            sb.append("\"");
            sb.append(":");
            sb.append(attributes.get(key).toJson());
            entryCount += 1;
        }
        sb.append("}");
        // WARNING: should not pretty-print this. it will cause a lot of problems
        return sb.toString();
    }

    @Override
    public String toJson(boolean prettyPrint) {
        if (prettyPrint) {
            return JsonUtil.prettyPrint(toJson());
        } else {
            return toJson();
        }
    }

    private String toSimplifiedJson() {
        return this.toSimplifiedJson(new SortKeys());
    }

    private String toSimplifiedJson(SortKeys sortKeys) {
        final StringBuilder sb = new StringBuilder();
        int entryCount = 0;
        sb.append("{");
        List<String> keys = getSortedKeys(attributes, sortKeys);
        for (String key : keys) {
            if (entryCount > 0) {
                sb.append(", ");// comma followed by a white space
            }
            sb.append("\"");
            sb.append(JsonUtil.escapeAsJsonString(key));
            sb.append("\"");
            sb.append(":");
            MetadataAttribute attribute = attributes.get(key);
            sb.append("\"");
            sb.append(attribute.getValue());
            sb.append("\"");
            entryCount += 1;
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public String toString() {
        return toJson();
    }

    @Override
    public URL toURL() throws MaterialstoreException {
        return this.url;
    }

    @Override
    public String toURLAsString() throws MaterialstoreException {
        if (this.toURL() != null) {
            if (attributes.containsKey(KEY_URL_FRAGMENT)) {
                return Objects.requireNonNull(toURL()).toExternalForm() + "#" + attributes.get(KEY_URL_FRAGMENT).getValue();
            } else {
                return Objects.requireNonNull(toURL()).toExternalForm();
            }
        } else {
            return "";
        }
    }
}
