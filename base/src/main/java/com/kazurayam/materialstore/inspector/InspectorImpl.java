package com.kazurayam.materialstore.inspector;

import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.filesystem.metadata.SortKeys;
import com.kazurayam.materialstore.reduce.DiffingMPGProcessor;
import com.kazurayam.materialstore.reduce.MPGProcessor;
import com.kazurayam.materialstore.reduce.MProductGroup;
import com.kazurayam.materialstore.report.MProductGroupReporter;
import com.kazurayam.materialstore.report.MProductGroupReporterImpl;
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
    public MProductGroupReporter newMProductGroupReporter() throws MaterialstoreException {
        MProductGroupReporterImpl instance = new MProductGroupReporterImpl(store);
        instance.enablePrettyPrinting(true);
        return instance;
    }

    @Override
    public void setSortKeys(SortKeys sortKeys) {
        this.sortKeys = sortKeys;
    }

    @Override
    public MProductGroup process(MProductGroup input) throws MaterialstoreException {
        MProductGroup tmp = new MProductGroup(input);
        tmp = reducer.process(tmp);
        tmp.sort(sortKeys);
        return tmp;
    }

    @Override
    public Path report(MProductGroup mProductGroup, Double criteria, String fileName) throws MaterialstoreException {
        Objects.requireNonNull(mProductGroup);
        Objects.requireNonNull(criteria);
        Objects.requireNonNull(fileName);
        //
        MProductGroupReporter reporter = this.newMProductGroupReporter();
        reporter.setCriteria(criteria);
        reporter.report(mProductGroup, sortKeys, fileName);
        return store.getRoot().resolve(fileName);
    }

    @Override
    public Path report(MaterialList materialList, String fileName) throws MaterialstoreException {
        Objects.requireNonNull(materialList);
        Objects.requireNonNull(fileName);
        MaterialListReporterImpl reporter = new MaterialListReporterImpl(store);
        return reporter.report(materialList, sortKeys, fileName);
    }

}
