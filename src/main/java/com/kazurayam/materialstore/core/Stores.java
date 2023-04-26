package com.kazurayam.materialstore.core;

import java.nio.file.Path;

/**
 * Factory class for the Store interface
 */
public final class Stores {

    private Stores() {
        throw new UnsupportedOperationException("should not instantiate this");
    }

    public static Store newInstance(Path root) {
        return new StoreImpl(root);
    }

}
