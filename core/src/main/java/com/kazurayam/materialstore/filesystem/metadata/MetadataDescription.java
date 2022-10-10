package com.kazurayam.materialstore.filesystem.metadata;

import java.util.Objects;

public class MetadataDescription implements Comparable<MetadataDescription> {

    private final String representation;

    public MetadataDescription(String representation) {
        Objects.requireNonNull(representation);
        this.representation = representation;
    }

    public String getRepresentation() {
        return this.representation;
    }

    public String toString() {
        return this.representation;
    }

    public int compareTo(MetadataDescription other) {
        return this.representation.compareTo(other.representation);
    }
}
