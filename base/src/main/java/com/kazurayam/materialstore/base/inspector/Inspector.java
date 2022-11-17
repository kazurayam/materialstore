package com.kazurayam.materialstore.base.inspector;

import com.kazurayam.materialstore.core.filesystem.MaterialList;
import com.kazurayam.materialstore.core.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.core.filesystem.Store;
import com.kazurayam.materialstore.core.filesystem.SortKeys;
import com.kazurayam.materialstore.base.reduce.MaterialProductGroup;
import com.kazurayam.materialstore.base.report.MaterialProductGroupReporter;

import java.nio.file.Path;

public abstract class Inspector {

    /*
     * the factory method
     */
    public static Inspector newInstance(Store store) {
        return new InspectorImpl(store);
    }

    public abstract MaterialProductGroupReporter newMaterialProductGroupReporter() throws MaterialstoreException;

    public abstract void setSortKeys(SortKeys sortKeys);

    public abstract MaterialProductGroup reduceAndSort(MaterialProductGroup input) throws MaterialstoreException;

    public abstract String resolveReportFileName(MaterialProductGroup mpg);

    public abstract Path report(MaterialProductGroup mpg, Double threshold) throws MaterialstoreException;

    public abstract String resolveReportFileName(MaterialList materialList);

    public abstract Path report(MaterialList materialList) throws MaterialstoreException;

}
