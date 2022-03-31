package com.kazurayam.materialstore.reduce.differ;

import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.reduce.MaterialProduct;

public interface Differ {

    MaterialProduct injectDiff(MaterialProduct mProduct) throws MaterialstoreException;

    void setStore(Store store);

}
