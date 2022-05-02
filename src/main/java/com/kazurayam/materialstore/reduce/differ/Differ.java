package com.kazurayam.materialstore.reduce.differ;

import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.zip.MaterialProduct;

public interface Differ {

    MaterialProduct injectDiff(MaterialProduct mProduct) throws MaterialstoreException;

}
