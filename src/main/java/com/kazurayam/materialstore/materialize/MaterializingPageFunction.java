package com.kazurayam.materialstore.materialize;

import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.Material;

@FunctionalInterface
public interface MaterializingPageFunction<Target, WebDriver, StorageDirectory, Material> {

    Material accept(Target target, WebDriver driver, StorageDirectory storageDirectory)
            throws MaterialstoreException;

}
