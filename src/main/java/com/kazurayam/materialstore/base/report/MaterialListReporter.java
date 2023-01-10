package com.kazurayam.materialstore.base.report;

import com.kazurayam.materialstore.core.MaterialList;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.SortKeys;

import java.nio.file.Path;

public abstract class MaterialListReporter extends AbstractReporter {

    public abstract Path report(MaterialList materialList, String fileName) throws MaterialstoreException;

    public abstract Path report(MaterialList materialList, SortKeys sortKeys, String fileName) throws MaterialstoreException;

    public abstract void report(MaterialList materialList, Path filePath) throws MaterialstoreException;

    public abstract void report(MaterialList materialList, SortKeys sortKeys, Path filePath) throws MaterialstoreException;

}
