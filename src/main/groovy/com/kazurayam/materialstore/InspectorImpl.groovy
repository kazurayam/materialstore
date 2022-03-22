package com.kazurayam.materialstore


import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.MaterialList
import com.kazurayam.materialstore.report.MProductGroupBasicReporterMB
import com.kazurayam.materialstore.report.MProductGroupReporter
import com.kazurayam.materialstore.report.MaterialListBasicReporterMB
import com.kazurayam.materialstore.reduce.MProductGroup
import com.kazurayam.materialstore.reduce.Reducer
import com.kazurayam.materialstore.reduce.DifferDriverImpl
import com.kazurayam.materialstore.filesystem.Store

import java.nio.file.Path

class InspectorImpl extends Inspector {

    private final Store store
    private final Reducer reducer

    InspectorImpl(Store store) {
        this.store = store
        this.reducer = new DifferDriverImpl.Builder(store.getRoot()).build()
    }

    @Override
    MProductGroupReporter newReporter(JobName jobName) {
        return new MProductGroupBasicReporterMB(store, jobName)
    }

    @Override
    MProductGroup reduce(MProductGroup input) {
        MProductGroup tmp = new MProductGroup(input)
        tmp = reducer.reduce(tmp)
        tmp.sort()
        return tmp
    }

    @Override
    Path report(MProductGroup mProductGroup, Double criteria, String fileName) {
        Objects.requireNonNull(mProductGroup)
        MProductGroupReporter reporter = this.newReporter(mProductGroup.getJobName())
        reporter.setCriteria(criteria)
        reporter.report(mProductGroup, fileName)
        return store.getRoot().resolve(fileName)
    }

    @Override
    void report(MProductGroup mProductGroup, Double criteria, Path filePath) {
        Objects.requireNonNull(mProductGroup)
        MProductGroupReporter reporter = this.newReporter(mProductGroup.getJobName())
        reporter.setCriteria(criteria)
        reporter.report(mProductGroup, filePath)
    }

    @Override
    Path report(MaterialList materialList,
                         String fileName = "list.html") {
        Objects.requireNonNull(materialList)
        Objects.requireNonNull(fileName)
        MaterialListBasicReporterMB reporter =
                new MaterialListBasicReporterMB(store, materialList.getJobName())
        return reporter.report(materialList, fileName)
    }

    @Override
    void report(MaterialList materialList, Path filePath) {
        Objects.requireNonNull(materialList)
        Objects.requireNonNull(filePath)
        MaterialListBasicReporterMB reporter =
                new MaterialListBasicReporterMB(store, materialList.getJobName())
        reporter.report(materialList, filePath)
    }

}
