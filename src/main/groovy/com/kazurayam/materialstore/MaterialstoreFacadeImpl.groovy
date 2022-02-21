package com.kazurayam.materialstore


import com.kazurayam.materialstore.differ.DiffReporter
import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.MaterialList
import com.kazurayam.materialstore.reporter.ArtifactGroupBasicReporter
import com.kazurayam.materialstore.reporter.MaterialsBasicReporter
import com.kazurayam.materialstore.resolvent.ArtifactGroup
import com.kazurayam.materialstore.resolvent.Resolvent
import com.kazurayam.materialstore.differ.DifferDriverImpl
import com.kazurayam.materialstore.filesystem.Store

import java.nio.file.Path

class MaterialstoreFacadeImpl extends MaterialstoreFacade {

    private final Store store
    private final List<Resolvent> resolventList

    MaterialstoreFacadeImpl(Store store) {
        this.store = store
        this.resolventList = new ArrayList<>()
        //
        resolventList.add(new DifferDriverImpl.Builder(store.getRoot()).build())
    }

    @Override
    void addResolvent(Resolvent resolvent) {
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
        return new ArtifactGroupBasicReporter(getRoot(), jobName)
    }

    @Override
    Path reportArtifactGroup(JobName jobName, ArtifactGroup artifactGroup,
                             Double criteria, String fileName) {
        DiffReporter reporter = this.newReporter(jobName)
        reporter.setCriteria(criteria)
        reporter.reportDiffs(artifactGroup, fileName)
        return root.resolve(fileName)
    }

    @Override
    Path reportMaterials(JobName jobName, MaterialList materialList,
                         String fileName = "list.html") {
        Objects.requireNonNull(jobName)
        Objects.requireNonNull(materialList)
        Objects.requireNonNull(fileName)
        MaterialsBasicReporter reporter =
                new MaterialsBasicReporter(this.root, jobName)
        return reporter.reportMaterials(materialList, fileName)
    }

    @Override
    ArtifactGroup workOn(ArtifactGroup input) {
        ArtifactGroup tmp = new ArtifactGroup(input)
        resolventList.each {resolvent ->
            tmp = apply(tmp, resolvent)
        }
        tmp.sort()
        return tmp
    }
}
