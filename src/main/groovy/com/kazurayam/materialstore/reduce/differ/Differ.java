package com.kazurayam.materialstore.reduce.differ;

import com.kazurayam.materialstore.reduce.MaterialProduct;

import java.nio.file.Path;

public interface Differ {

    MaterialProduct makeMProduct(MaterialProduct mProduct);

    void setRoot(Path root);

}
