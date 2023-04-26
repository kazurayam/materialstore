package com.kazurayam.materialstore.base.reduce;

import com.kazurayam.materialstore.core.MaterialstoreException;

public interface MPGProcessor {

    MaterialProductGroup process(MaterialProductGroup mpg) throws MaterialstoreException;

}
