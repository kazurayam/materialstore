package com.kazurayam.materialstore.reduce;

import com.kazurayam.materialstore.MaterialstoreException;

public interface Reducer {

    MProductGroup reduce(MProductGroup mProductGroup) throws MaterialstoreException;

}
