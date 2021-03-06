package com.kazurayam.materialstore.materialize;

import com.kazurayam.materialstore.filesystem.MaterialstoreException;

@FunctionalInterface
public interface MaterializingPageFunction<Target, WebDriver, StorageDirectory, Material> {

    Material accept(Target target, WebDriver driver, StorageDirectory storageDirectory)
            throws MaterialstoreException;

}
