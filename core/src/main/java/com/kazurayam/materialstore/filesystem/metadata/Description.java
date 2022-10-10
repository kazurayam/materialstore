package com.kazurayam.materialstore.filesystem.metadata;

import java.util.Objects;

public abstract class Description {

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
}
