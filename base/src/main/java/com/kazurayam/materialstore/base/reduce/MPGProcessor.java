package com.kazurayam.materialstore.base.reduce;

import com.kazurayam.materialstore.core.filesystem.MaterialstoreException;

public interface MPGProcessor {

    MaterialProductGroup process(MaterialProductGroup mpg) throws MaterialstoreException;

}
