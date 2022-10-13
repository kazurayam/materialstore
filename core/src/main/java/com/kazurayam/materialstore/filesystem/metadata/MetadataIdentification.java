package com.kazurayam.materialstore.filesystem.metadata;

import java.util.Objects;

public class MetadataIdentification implements Comparable<MetadataIdentification> {

    private final String representation;

    public MetadataIdentification(String representation) {
        Objects.requireNonNull(representation);
        this.representation = representation;
    }

    public String getRepresentation() {
        return this.representation;
    }

    public String toString() {
        return this.representation;
    }

    public int compareTo(MetadataIdentification other) {
        return this.representation.compareTo(other.representation);
    }
}
