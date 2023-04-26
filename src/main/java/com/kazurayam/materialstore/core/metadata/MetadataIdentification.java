package com.kazurayam.materialstore.core.metadata;

import java.util.Objects;

public class MetadataIdentification implements Comparable<MetadataIdentification> {

    private final String identification;

    public MetadataIdentification(String representation) {
        Objects.requireNonNull(representation);
        this.identification = representation;
    }

    public String getIdentification() {
        return this.identification;
    }

    public String toString() {
        return this.identification;
    }

    public int compareTo(MetadataIdentification other) {
        return this.identification.compareTo(other.identification);
    }
}
