package com.kazurayam.materialstore.base.reduce.differ;

import com.kazurayam.materialstore.base.reduce.zipper.MaterialProduct;
import com.kazurayam.materialstore.core.filesystem.MaterialstoreException;

public interface Differ {

    MaterialProduct stuffDiff(MaterialProduct mProduct) throws MaterialstoreException;

}
