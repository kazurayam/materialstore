package com.kazurayam.materialstore.report;

import com.kazurayam.materialstore.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.reduce.MProductGroup;

import java.nio.file.Path;

public abstract class MProductGroupReporter extends AbstractReporter {
    /**
     * @param criteria the diffRatio of each MaterialProduct object is compared against
     *                 the criteria. if the diffRatio &gt; the criteria, the MaterialProduct is regarded
     *                 "TO BE WARNED". The criteria is set to be 0.0 as default.
     */
    public abstract void setCriteria(Double criteria);

    public abstract Path report(MProductGroup mProductGroup, String fileName) throws MaterialstoreException;

    public abstract void report(MProductGroup mProductGroup, Path filePath) throws MaterialstoreException;
}
