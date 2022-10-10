package com.kazurayam.materialstore.filesystem.metadata;

import java.util.Objects;

public abstract class Description implements Comparable<Description> {

    private final String representation;

    public Description(String representation) {
        Objects.requireNonNull(representation);
        this.representation = representation;
    }

    public String getRepresentation() {
        return this.representation;
    }

    public String toString() {
        return this.representation;
    }

    public int compareTo(Description other) {
        return this.representation.compareTo(other.representation);
    }
}
