package com.kazurayam.materialstore

import com.kazurayam.materialstore.diffartifact.DiffArtifactGroup
import com.kazurayam.materialstore.differ.DifferDriver
import com.kazurayam.materialstore.differ.DifferDriverImpl
import com.kazurayam.materialstore.filesystem.Store

class MaterialstoreFacade {

    private final Store store

    MaterialstoreFacade(Store store) {
        this.store = store
    }

    DiffArtifactGroup makeDiff(DiffArtifactGroup diffArtifactGroup) {
        // prepare the default DifferDriver
        DifferDriver differDriver = new DifferDriverImpl.Builder(store.getRoot()).build()
        return makeDiff(diffArtifactGroup, differDriver)
    }

    static DiffArtifactGroup makeDiff(DiffArtifactGroup diffArtifactGroup, DifferDriver differDriver) {
        Objects.requireNonNull(diffArtifactGroup)
        Objects.requireNonNull(differDriver)
        // perform diff-ing, stuff the result in the DiffArtifact collection
        diffArtifactGroup.applyResolvent(differDriver)
        // sort the collection for better presentation
        diffArtifactGroup.sort()
        return diffArtifactGroup
    }
}
