package com.kazurayam.materialstore.store

import java.nio.file.Path

/**
 * Factory class for the Store interface
 */
class Stores {

    private Stores() { throw new UnsupportedOperationException("should not instantiate this") }

    static Store newInstance(Path root) {
        return new StoreImpl(root)
    }

}
