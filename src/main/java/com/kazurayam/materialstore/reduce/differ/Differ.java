package com.kazurayam.materialstore.reduce.differ;

import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.reduce.zip.zip.MaterialProduct;

public interface Differ {

    MaterialProduct stuffDiff(MaterialProduct mProduct) throws MaterialstoreException;

}
