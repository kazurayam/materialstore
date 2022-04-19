package com.kazurayam.materialstore.materialize;

import com.kazurayam.materialstore.MaterialstoreException;

public interface MaterializingWebResourceFunction<Target, StorageDirectory> {

    void accept(Target target, StorageDirectory storageDirectory)
            throws MaterialstoreException;
}
