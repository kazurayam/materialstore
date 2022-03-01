package com.kazurayam.materialstore


import com.kazurayam.materialstore.differ.DiffReporter
import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.MaterialList
import com.kazurayam.materialstore.report.MProductGroupBasicReporter
import com.kazurayam.materialstore.report.MaterialsBasicReporter
import com.kazurayam.materialstore.reduce.MProductGroup
import com.kazurayam.materialstore.reduce.Reducer
import com.kazurayam.materialstore.differ.DifferDriverImpl
import com.kazurayam.materialstore.filesystem.Store

import java.nio.file.Path

class MaterialstoreFacadeImpl extends MaterialstoreFacade {

    private final Store store
    private final List<Reducer> resolventList

    MaterialstoreFacadeImpl(Store store) {
        this.store = store
        this.resolventList = new ArrayList<>()
        resolventList.add(new DifferDriverImpl.Builder(store.getRoot()).build())
    }

    @Override
    void addResolvent(Reducer resolvent) {
        Objects.requireNonNull(resolvent)
        resolventList.add(resolvent)
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
        return new MProductGroupBasicReporter(getRoot(), jobName)
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
        MaterialsBasicReporter reporter =
                new MaterialsBasicReporter(this.root, jobName)
        return reporter.reportMaterials(materialList, fileName)
    }

    @Override
    MProductGroup reduce(MProductGroup input) {
        MProductGroup tmp = new MProductGroup(input)
        resolventList.each {resolvent ->
            tmp = resolvent.reduce(tmp)
        }
        tmp.sort()
        return tmp
    }

}
