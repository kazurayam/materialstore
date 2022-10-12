package com.kazurayam.materialstore.report;

import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.metadata.SortKeys;

import java.nio.file.Path;

public abstract class MaterialListReporter extends AbstractReporter {

    public abstract Path report(MaterialList materialList, String fileName) throws MaterialstoreException;

    public abstract Path report(MaterialList materialList, SortKeys sortKeys, String fileName) throws MaterialstoreException;

    public abstract void report(MaterialList materialList, Path filePath) throws MaterialstoreException;

    public abstract void report(MaterialList materialList, SortKeys sortKeys, Path filePath) throws MaterialstoreException;

}
