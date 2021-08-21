package com.kazurayam.materialstore

import java.nio.file.Path

/**
 * Factory class for the Store interface
 */
final class Stores {

    private Stores() { throw new UnsupportedOperationException("should not instantiate this") }

    static Store newInstance(Path root) {
        return new StoreImpl(root)
    }

}
