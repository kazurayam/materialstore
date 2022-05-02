package com.kazurayam.materialstore.reduce;

import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.FileType;

public interface DifferDriver extends Reducer {

    MProductGroup differentiate(MProductGroup mProductGroup) throws MaterialstoreException;

    boolean hasDiffer(FileType fileType);
}
