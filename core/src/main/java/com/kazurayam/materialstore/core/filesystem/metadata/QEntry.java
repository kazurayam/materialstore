package com.kazurayam.materialstore.core.filesystem.metadata;

import com.kazurayam.materialstore.core.filesystem.Metadata;

/**
 * a pair of Key-Value in the QueryOnMetadata object.
 * This class implements boolean matches(Metadata) method, which works
 * as a helper for QueryOnMetadataImpl#matches(Metadata) method.
 */
public final class QEntry implements Comparable<QEntry> {

    private final String key;
    private final QValue query;

    public QEntry(String key, QValue qValue) {
        this.key = key;
        this.query = qValue;
    }

    public String getKey() {
        return this.key;
    }

    public QValue getQValue() {
        return this.query;
    }

    public boolean matches(final Metadata metadata) {
        if (this.key.equals("*")) {
            boolean found = false;
            for (String key : metadata.keySet()) {
                if (QEntry.this.getQValue().matches(metadata.get(key))) {
                    found = true;
                }
            }
            return found;
        } else if (metadata.containsKey(key)) {
            return this.query.matches(metadata.get(key));
        } else {
            return false;
        }
    }

    @Override
    public int compareTo(QEntry other) {
        int keyComp = key.compareTo(other.getKey());
        if (keyComp != 0) {
            return query.compareTo(other.getQValue());
        } else {
            return keyComp;
        }
    }
}