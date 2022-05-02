package com.kazurayam.materialstore.reduce;

import com.kazurayam.materialstore.MaterialstoreException;

public interface MPGProcessor {

    MProductGroup process(MProductGroup mProductGroup) throws MaterialstoreException;

}
