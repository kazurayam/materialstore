package com.kazurayam.materialstore


import com.kazurayam.materialstore.resolvent.ArtifactGroup
import com.kazurayam.materialstore.resolvent.Resolvent
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

    ArtifactGroup workOn(ArtifactGroup input) {
        ArtifactGroup tmp = new ArtifactGroup(input)
        resolventList.each {resolvent ->
            tmp = apply(tmp, resolvent)
        }
        tmp.sort()
        return tmp
    }

    static ArtifactGroup apply(ArtifactGroup artifactGroup, Resolvent resolvent) {
        Objects.requireNonNull(artifactGroup)
        Objects.requireNonNull(resolvent)
        // perform diff-ing, stuff the result in the Artifact collection
        return resolvent.resolve(artifactGroup)
    }
}
