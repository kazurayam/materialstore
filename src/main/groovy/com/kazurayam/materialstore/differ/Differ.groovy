package com.kazurayam.materialstore.differ


import com.kazurayam.materialstore.reduce.MProduct

import java.nio.file.Path

interface Differ {

    MProduct makeMProduct(MProduct mProduct)

    void setRoot(Path root)

}
