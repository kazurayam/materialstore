package com.kazurayam.materialstore


import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.MaterialList
import com.kazurayam.materialstore.report.MProductGroupReporterImpl
import com.kazurayam.materialstore.report.MProductGroupReporter
import com.kazurayam.materialstore.reduce.MProductGroup
import com.kazurayam.materialstore.reduce.Reducer
import com.kazurayam.materialstore.reduce.DifferDriverImpl
import com.kazurayam.materialstore.filesystem.Store
import com.kazurayam.materialstore.report.MaterialListReporterImplMB

import java.nio.file.Path

class InspectorImpl extends Inspector {

    private final Store store
    private final Reducer reducer

    InspectorImpl(Store store) {
        this.store = store
        this.reducer = new DifferDriverImpl.Builder(store).build()
    }

    @Override
    MProductGroupReporter newReporter(JobName jobName) {
        return new MProductGroupReporterImpl(store, jobName)
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
        MaterialListReporterImplMB reporter =
                new MaterialListReporterImplMB(store, materialList.getJobName())
        return reporter.report(materialList, fileName)
    }

    @Override
    void report(MaterialList materialList, Path filePath) {
        Objects.requireNonNull(materialList)
        Objects.requireNonNull(filePath)
        MaterialListReporterImplMB reporter =
                new MaterialListReporterImplMB(store, materialList.getJobName())
        reporter.report(materialList, filePath)
    }

}
