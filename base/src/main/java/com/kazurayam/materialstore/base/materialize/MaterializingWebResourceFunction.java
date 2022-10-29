package com.kazurayam.materialstore.base.materialize;

import com.kazurayam.materialstore.core.filesystem.MaterialstoreException;

public interface MaterializingWebResourceFunction<Target, StorageDirectory, Material> {

    Material accept(Target target, StorageDirectory storageDirectory)
            throws MaterialstoreException;
}
