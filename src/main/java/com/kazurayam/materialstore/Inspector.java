package com.kazurayam.materialstore;

import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.filesystem.Store;
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

    public abstract MProductGroupReporter newMProductGroupReporter(JobName jobName) throws MaterialstoreException;

    public abstract MProductGroup reduce(MProductGroup input) throws MaterialstoreException;

    public abstract Path report(MProductGroup mProductGroup, Double criteria, String fileName) throws MaterialstoreException;

    public abstract void report(MProductGroup mProductGroup, Double criteria, Path filePath) throws MaterialstoreException;

    public abstract Path report(MaterialList materialList, String fileName) throws MaterialstoreException;

    public abstract void report(MaterialList materialList, Path filePath) throws MaterialstoreException;
}
