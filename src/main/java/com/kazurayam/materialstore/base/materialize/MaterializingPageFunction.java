package com.kazurayam.materialstore.base.materialize;

import com.kazurayam.materialstore.core.filesystem.MaterialstoreException;

@FunctionalInterface
public interface MaterializingPageFunction<Target, WebDriver, StorageDirectory, Map, Material> {

    Material accept(Target target, WebDriver driver, StorageDirectory storageDirectory,
                    Map attributes) throws MaterialstoreException;
}
