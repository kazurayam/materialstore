package com.kazurayam.materialstore.filesystem.metadata;

import com.kazurayam.materialstore.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.Metadata;
import com.kazurayam.materialstore.filesystem.QueryOnMetadata;
import com.kazurayam.materialstore.filesystem.SortKeys;
import com.kazurayam.materialstore.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
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

    public MetadataImpl(Map<String, MetadataAttribute> attributes) {
        this.attributes = attributes;
    }

    @Override
    public boolean containsKey(String key) {
        return attributes.containsKey(key);
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
    public MetadataAttribute getMetadataAttribute(String key) {
        return attributes.get(key);
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
    public int size() {
        return attributes.size();
    }

    @Override
    public String toURLAsString() throws MaterialstoreException {
        if (attributes.containsKey(KEY_URL_FRAGMENT)) {
            return Objects.requireNonNull(toURL()).toExternalForm() + "#" + attributes.get(KEY_URL_FRAGMENT).getValue();
        } else {
            return Objects.requireNonNull(toURL()).toExternalForm();
        }

    }

    @Override
    public URL toURL() throws MaterialstoreException {
        if (attributes.containsKey(KEY_URL_PROTOCOL) && attributes.containsKey(KEY_URL_HOST)) {
            StringBuilder sb = new StringBuilder();
            sb.append(attributes.get(KEY_URL_PROTOCOL).getValue());
            sb.append(":");
            if (attributes.get(KEY_URL_PROTOCOL).getValue().startsWith("http")) {
                sb.append("//");
            }

            sb.append(attributes.get(KEY_URL_HOST).getValue());
            if (attributes.containsKey(KEY_URL_PORT) && !attributes.get(KEY_URL_PORT).getValue().equals("80")) {
                sb.append(":");
                sb.append(attributes.get(KEY_URL_PORT).getValue());
            }

            sb.append(attributes.get(KEY_URL_PATH).getValue());
            if (attributes.containsKey(KEY_URL_QUERY)) {
                sb.append("?");
                sb.append(attributes.get(KEY_URL_QUERY).getValue());
            }

            try {
                return new URL(sb.toString());
            } catch (MalformedURLException e) {
                throw new MaterialstoreException(e);
            }
        } else {
            try {
                return new URL("file://null_object");
            } catch (MalformedURLException e) {
                throw new MaterialstoreException(e);
            }
        }

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
    public boolean matchesByAster(QueryOnMetadata query, String key) {
        return query.containsKey("*") && query.get("*").matches(this.get(key));
    }

    @Override
    public boolean matchesIndividually(QueryOnMetadata query, String key) {
        return query.containsKey(key) && this.containsKey(key) && query.get(key).matches(this.get(key));
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
    public MetadataIdentification getMetadataIdentification() {
        return this.getMetadataIdentification(new SortKeys());
    }

    @Override
    public MetadataIdentification getMetadataIdentification(SortKeys sortKeys) {
        String simplifiedJson = this.toSimplifiedJson(sortKeys);
        return new MetadataIdentification(simplifiedJson);
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

    @Override
    public String toString() {
        return toJson();
    }

    /**
     * compares this.toSimplifiedJson() with other.toSimplifiedJson()
     * @param obj
     * @return
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
    public int hashCode() {
        int hash = 7;
        for (String key : this.keySet()) {
            hash = 31 * hash + key.hashCode();
            hash = 31 * hash + Objects.requireNonNull(this.get(key)).hashCode();
        }
        return hash;
    }

    @Override
    public int compareTo(Metadata obj) {
        MetadataImpl other = (MetadataImpl) (obj);
        return this.toString().compareTo(other.toString());
    }
}
