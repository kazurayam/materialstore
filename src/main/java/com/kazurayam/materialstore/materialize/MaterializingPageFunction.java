package com.kazurayam.materialstore.materialize;

import com.kazurayam.materialstore.MaterialstoreException;

@FunctionalInterface
public interface MaterializingPageFunction<Target, WebDriver, StorageDirectory> {

    void accept(Target target, WebDriver driver, StorageDirectory storageDirectory)
            throws MaterialstoreException;

}
