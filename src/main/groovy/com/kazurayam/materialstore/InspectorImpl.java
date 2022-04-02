package com.kazurayam.materialstore;

import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.reduce.DifferDriverImpl;
import com.kazurayam.materialstore.reduce.MProductGroup;
import com.kazurayam.materialstore.reduce.Reducer;
import com.kazurayam.materialstore.report.MProductGroupReporter;
import com.kazurayam.materialstore.report.MProductGroupReporterImpl;
import com.kazurayam.materialstore.report.MaterialListReporterImplMB;

import java.nio.file.Path;
import java.util.Objects;

public class InspectorImpl extends Inspector {
    public InspectorImpl(Store store) {
        this.store = store;
        this.reducer = new DifferDriverImpl.Builder(store).build();
    }

    @Override
    public MProductGroupReporter newReporter(JobName jobName) throws MaterialstoreException {
        return new MProductGroupReporterImpl(store, jobName);
    }

    @Override
    public MProductGroup reduce(MProductGroup input) throws MaterialstoreException {
        MProductGroup tmp = new MProductGroup(input);
        tmp = reducer.reduce(tmp);
        tmp.sort();
        return tmp;
    }

    @Override
    public Path report(MProductGroup mProductGroup, Double criteria, String fileName) throws MaterialstoreException {
        Objects.requireNonNull(mProductGroup);
        MProductGroupReporter reporter = this.newReporter(mProductGroup.getJobName());
        reporter.setCriteria(criteria);
        reporter.report(mProductGroup, fileName);
        return store.getRoot().resolve(fileName);
    }

    @Override
    public void report(MProductGroup mProductGroup, Double criteria, Path filePath) throws MaterialstoreException {
        Objects.requireNonNull(mProductGroup);
        MProductGroupReporter reporter = this.newReporter(mProductGroup.getJobName());
        reporter.setCriteria(criteria);
        reporter.report(mProductGroup, filePath);
    }

    @Override
    public Path report(MaterialList materialList, String fileName) {
        Objects.requireNonNull(materialList);
        Objects.requireNonNull(fileName);
        MaterialListReporterImplMB reporter = new MaterialListReporterImplMB(store, materialList.getJobName());
        return reporter.report(materialList, fileName);
    }

    @Override
    public void report(MaterialList materialList, Path filePath) {
        Objects.requireNonNull(materialList);
        Objects.requireNonNull(filePath);
        MaterialListReporterImplMB reporter = new MaterialListReporterImplMB(store, materialList.getJobName());
        reporter.report(materialList, filePath);
    }

    private final Store store;
    private final Reducer reducer;
}