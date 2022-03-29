package com.kazurayam.materialstore.reduce.differ;

import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.reduce.MaterialProduct;

import java.nio.file.Path;

public interface Differ {

    MaterialProduct makeMProduct(MaterialProduct mProduct) throws MaterialstoreException;

    void setRoot(Path root);

}
