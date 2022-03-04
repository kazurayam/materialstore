package com.kazurayam.materialstore


import com.kazurayam.materialstore.reduce.differ.DiffReporter
import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.MaterialList
import com.kazurayam.materialstore.report.MProductGroupBasicReporter
import com.kazurayam.materialstore.report.MaterialListBasicReporter
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
    DiffReporter newReporter(JobName jobName) {
        return new MProductGroupBasicReporter(store, jobName)
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
        DiffReporter reporter = this.newReporter(mProductGroup.getJobName())
        reporter.setCriteria(criteria)
        reporter.reportDiffs(mProductGroup, fileName)
        return store.getRoot().resolve(fileName)
    }

    @Override
    Path report(MaterialList materialList,
                         String fileName = "list.html") {
        Objects.requireNonNull(materialList)
        Objects.requireNonNull(fileName)
        MaterialListBasicReporter reporter =
                new MaterialListBasicReporter(store, materialList.getJobName())
        return reporter.report(materialList, fileName)
    }


}
