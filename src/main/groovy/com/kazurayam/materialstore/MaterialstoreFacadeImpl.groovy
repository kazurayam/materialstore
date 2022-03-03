package com.kazurayam.materialstore


import com.kazurayam.materialstore.reduce.differ.DiffReporter
import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.MaterialList
import com.kazurayam.materialstore.report.MProductGroupBasicReporter
import com.kazurayam.materialstore.report.MaterialListBasicReporter
import com.kazurayam.materialstore.reduce.MProductGroup
import com.kazurayam.materialstore.reduce.Reducer
import com.kazurayam.materialstore.reduce.differ.DifferDriverImpl
import com.kazurayam.materialstore.filesystem.Store

import java.nio.file.Path

class MaterialstoreFacadeImpl extends MaterialstoreFacade {

    private final Store store
    private final List<Reducer> reducerList

    MaterialstoreFacadeImpl(Store store) {
        this.store = store
        this.reducerList = new ArrayList<>()
        reducerList.add(new DifferDriverImpl.Builder(store.getRoot()).build())
    }

    @Override
    void addResolvent(Reducer reducer) {
        Objects.requireNonNull(reducer)
        reducerList.add(reducer)
    }

    @Override
    Path getRoot() {
        return getStore().getRoot()
    }

    @Override
    Store getStore() {
        return this.store
    }

    @Override
    DiffReporter newReporter(JobName jobName) {
        return new MProductGroupBasicReporter(getStore(), jobName)
    }

    @Override
    Path report(JobName jobName, MProductGroup mProductGroup,
                Double criteria, String fileName) {
        DiffReporter reporter = this.newReporter(jobName)
        reporter.setCriteria(criteria)
        reporter.reportDiffs(mProductGroup, fileName)
        return root.resolve(fileName)
    }

    @Override
    Path report(JobName jobName, MaterialList materialList,
                         String fileName = "list.html") {
        Objects.requireNonNull(jobName)
        Objects.requireNonNull(materialList)
        Objects.requireNonNull(fileName)
        MaterialListBasicReporter reporter =
                new MaterialListBasicReporter(store, jobName)
        return reporter.report(materialList, fileName)
    }

    @Override
    MProductGroup reduce(MProductGroup input) {
        MProductGroup tmp = new MProductGroup(input)
        reducerList.each { reducer ->
            tmp = reducer.reduce(tmp)
        }
        tmp.sort()
        return tmp
    }

}
