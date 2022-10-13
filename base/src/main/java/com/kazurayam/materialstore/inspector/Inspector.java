package com.kazurayam.materialstore.inspector;

import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.filesystem.metadata.SortKeys;
import com.kazurayam.materialstore.reduce.MProductGroup;
import com.kazurayam.materialstore.report.MProductGroupReporter;

import java.nio.file.Path;

public abstract class Inspector {
    /**
     * the factory method
     */
    public static Inspector newInstance(Store store) {
        return new InspectorImpl(store);
    }

    public abstract MProductGroupReporter newMProductGroupReporter() throws MaterialstoreException;

    public abstract void setSortKeys(SortKeys sortKeys);

    public abstract MProductGroup process(MProductGroup input) throws MaterialstoreException;

    public abstract Path report(MProductGroup mProductGroup, Double criteria, String fileName) throws MaterialstoreException;

    public abstract Path report(MaterialList materialList, String fileName) throws MaterialstoreException;

}
