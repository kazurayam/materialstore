package com.kazurayam.materialstore.reduce

import com.kazurayam.materialstore.filesystem.FileType


interface DifferDriver extends Reducer {

    MProductGroup differentiate(MProductGroup mProductGroup)

    MaterialProduct differentiate(MaterialProduct mProduct)

    boolean hasDiffer(FileType fileType)
}