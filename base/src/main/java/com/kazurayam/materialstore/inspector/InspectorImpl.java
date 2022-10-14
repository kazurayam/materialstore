package com.kazurayam.materialstore.inspector;

import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.filesystem.metadata.SortKeys;
import com.kazurayam.materialstore.reduce.DiffingMPGProcessor;
import com.kazurayam.materialstore.reduce.MPGProcessor;
import com.kazurayam.materialstore.reduce.MaterialProductGroup;
import com.kazurayam.materialstore.report.MaterialProductGroupReporter;
import com.kazurayam.materialstore.report.MaterialProductGroupReporterImpl;
import com.kazurayam.materialstore.report.MaterialListReporterImpl;

import java.nio.file.Path;
import java.util.Objects;

public class InspectorImpl extends Inspector {

    private final Store store;
    private final MPGProcessor reducer;

    private SortKeys sortKeys;

    public InspectorImpl(Store store) {
        this.store = store;
        this.reducer = new DiffingMPGProcessor.Builder(store).build();
        this.sortKeys = new SortKeys();
    }

    @Override
    public MaterialProductGroupReporter newMaterialProductGroupReporter() throws MaterialstoreException {
        MaterialProductGroupReporterImpl instance = new MaterialProductGroupReporterImpl(store);
        instance.enablePrettyPrinting(true);
        return instance;
    }

    @Override
    public void setSortKeys(SortKeys sortKeys) {
        this.sortKeys = sortKeys;
    }

    @Override
    public MaterialProductGroup process(MaterialProductGroup input) throws MaterialstoreException {
        MaterialProductGroup tmp = new MaterialProductGroup(input);
        tmp = reducer.process(tmp);
        tmp.order(sortKeys);
        return tmp;
    }


    @Override
    public Path report(MaterialList materialList, SortKeys sortKeys, String fileName) throws MaterialstoreException {
        Objects.requireNonNull(materialList);
        Objects.requireNonNull(sortKeys);
        Objects.requireNonNull(fileName);
        MaterialListReporterImpl reporter = new MaterialListReporterImpl(store);
        return reporter.report(materialList, sortKeys, fileName);
    }

    @Override
    public Path report(MaterialList materialList, String fileName) throws MaterialstoreException {
        return report(materialList, new SortKeys(), fileName);
    }

    @Override
    public Path report(MaterialProductGroup mpg, Double criteria, String fileName) throws MaterialstoreException {
        return report(mpg, new SortKeys(), criteria, fileName);
    }

    @Override
    public Path report(MaterialProductGroup mpg, SortKeys sortKeys, Double criteria, String fileName) throws MaterialstoreException {
        Objects.requireNonNull(mpg);
        Objects.requireNonNull(sortKeys);
        Objects.requireNonNull(criteria);
        Objects.requireNonNull(fileName);
        //
        MaterialProductGroupReporter reporter = this.newMaterialProductGroupReporter();
        reporter.setCriteria(criteria);
        reporter.report(mpg, sortKeys, fileName);
        return store.getRoot().resolve(fileName);
    }


}
