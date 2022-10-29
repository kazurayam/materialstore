package com.kazurayam.materialstore.base.report;

import com.kazurayam.materialstore.core.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.core.filesystem.SortKeys;
import com.kazurayam.materialstore.base.reduce.MaterialProductGroup;

import java.nio.file.Path;

public abstract class MaterialProductGroupReporter extends AbstractReporter {
    /**
     * @param criteria the diffRatio of each MaterialProduct object is compared against
     *                 the criteria. if the diffRatio &gt; the criteria, the MaterialProduct is regarded
     *                 "TO BE WARNED". The criteria is set to be 0.0 as default.
     */

    public abstract void report(MaterialProductGroup mpg, Path filePath) throws MaterialstoreException;

    public abstract void report(MaterialProductGroup mpg, SortKeys sortKeys, Path filePath) throws MaterialstoreException;

    public abstract Path report(MaterialProductGroup mpg, SortKeys sortKeys, String fileName) throws MaterialstoreException;

    public abstract Path report(MaterialProductGroup mpg, String fileName) throws MaterialstoreException;

    public abstract void setCriteria(Double criteria);

}
