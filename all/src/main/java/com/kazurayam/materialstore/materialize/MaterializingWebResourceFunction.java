package com.kazurayam.materialstore.materialize;

import com.kazurayam.materialstore.filesystem.MaterialstoreException;

public interface MaterializingWebResourceFunction<Target, StorageDirectory, Material> {

    Material accept(Target target, StorageDirectory storageDirectory)
            throws MaterialstoreException;
}
