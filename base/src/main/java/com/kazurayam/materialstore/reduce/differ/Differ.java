package com.kazurayam.materialstore.reduce.differ;

import com.kazurayam.materialstore.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.reduce.zipper.MaterialProduct;

public interface Differ {

    MaterialProduct stuffDiff(MaterialProduct mProduct) throws MaterialstoreException;

}
