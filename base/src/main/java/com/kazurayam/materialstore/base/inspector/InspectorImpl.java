package com.kazurayam.materialstore.base.inspector;

import com.kazurayam.materialstore.core.filesystem.MaterialList;
import com.kazurayam.materialstore.core.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.core.filesystem.Store;
import com.kazurayam.materialstore.core.filesystem.SortKeys;
import com.kazurayam.materialstore.base.reduce.DiffingMPGProcessor;
import com.kazurayam.materialstore.base.reduce.MPGProcessor;
import com.kazurayam.materialstore.base.reduce.MaterialProductGroup;
import com.kazurayam.materialstore.base.report.MaterialProductGroupReporter;
import com.kazurayam.materialstore.base.report.MaterialProductGroupReporterImpl;
import com.kazurayam.materialstore.base.report.MaterialListReporterImpl;

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
    public MaterialProductGroup reduceAndSort(MaterialProductGroup input) throws MaterialstoreException {
        MaterialProductGroup tmp = new MaterialProductGroup(input);
        tmp = reducer.process(tmp);
        tmp.order(sortKeys);
        return tmp;
    }

    @Override
    public String resolveReportFileName(MaterialList materialList) {
        return store.resolveReportFileName(materialList.getJobName(),
                materialList.getJobTimestamp());
    }

    @Override
    public String resolveReportFileName(MaterialProductGroup mpg) {
        Objects.requireNonNull(mpg);
        return store.resolveReportFileName(mpg.getJobName(),
                mpg.getJobTimestampOfReduceResult());
    }


    @Override
    public Path report(MaterialList materialList) throws MaterialstoreException {
        Objects.requireNonNull(materialList);
        String fileName = this.resolveReportFileName(materialList);
        return this.report(materialList, fileName);
    }

    private Path report(MaterialList materialList, String fileName) throws MaterialstoreException {
        Objects.requireNonNull(materialList);
        Objects.requireNonNull(fileName);
        MaterialListReporterImpl reporter = new MaterialListReporterImpl(store);
        return reporter.report(materialList, sortKeys, fileName);
    }

    @Override
    public Path report(MaterialProductGroup mpg, Double criteria) throws MaterialstoreException {
        Objects.requireNonNull(mpg);
        Objects.requireNonNull(criteria);
        String fileName = this.resolveReportFileName(mpg);
        return this.report(mpg, criteria, fileName);
    }

    private Path report(MaterialProductGroup mpg, Double criteria, String fileName) throws MaterialstoreException {
        Objects.requireNonNull(mpg);
        Objects.requireNonNull(criteria);
        Objects.requireNonNull(fileName);
        //
        MaterialProductGroupReporter reporter = this.newMaterialProductGroupReporter();
        reporter.setCriteria(criteria);
        reporter.report(mpg, sortKeys, fileName);
        return store.getRoot().resolve(fileName);
    }


}
