package com.kazurayam.materialstore

import com.kazurayam.materialstore.diffartifact.DiffArtifactGroup
import com.kazurayam.materialstore.diffartifact.Resolvent
import com.kazurayam.materialstore.differ.DifferDriver
import com.kazurayam.materialstore.differ.DifferDriverImpl
import com.kazurayam.materialstore.filesystem.Store

class MaterialstoreFacade {

    private final Store store
    private final List<Resolvent> resolventList

    MaterialstoreFacade(Store store) {
        this.store = store
        this.resolventList = new ArrayList<>()
        //
        resolventList.add(new DifferDriverImpl.Builder(store.getRoot()).build())
    }

    void addResolvent(Resolvent resolvent) {
        Objects.requireNonNull(resolvent)
        resolventList.add(resolvent)
    }

    DiffArtifactGroup workOn(DiffArtifactGroup input) {
        DiffArtifactGroup tmp = new DiffArtifactGroup(input)
        resolventList.each {resolvent ->
            tmp = apply(tmp, resolvent)
        }
        tmp.sort()
        return tmp
    }

    static DiffArtifactGroup apply(DiffArtifactGroup diffArtifactGroup, Resolvent resolvent) {
        Objects.requireNonNull(diffArtifactGroup)
        Objects.requireNonNull(resolvent)
        // perform diff-ing, stuff the result in the DiffArtifact collection
        diffArtifactGroup.applyResolvent(resolvent)
        return diffArtifactGroup
    }
}
