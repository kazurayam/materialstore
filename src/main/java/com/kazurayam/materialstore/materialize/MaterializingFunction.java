package com.kazurayam.materialstore.materialize;

import com.kazurayam.materialstore.MaterialstoreException;

@FunctionalInterface
public interface MaterializingFunction<Target, WebDriver, StorageDirectory> {

    void accept(Target targetURL, WebDriver driver, StorageDirectory storageDirectory) throws MaterialstoreException;

}
