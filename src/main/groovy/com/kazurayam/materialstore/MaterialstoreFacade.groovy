package com.kazurayam.materialstore

import com.kazurayam.materialstore.differ.DiffReporter
import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.MaterialList
import com.kazurayam.materialstore.resolvent.ArtifactGroup
import com.kazurayam.materialstore.resolvent.Resolvent
import com.kazurayam.materialstore.filesystem.Store
import com.kazurayam.materialstore.util.JsonUtil

import java.nio.file.Path

abstract class MaterialstoreFacade {

    /**
     * the factory method
     */
    static final MaterialstoreFacade newInstance(Store store) {
        return new MaterialstoreFacadeImpl(store)
    }

    abstract void addResolvent(Resolvent resolvent)

    abstract Path getRoot()

    abstract Store getStore()

    abstract DiffReporter newReporter(JobName jobName)

    abstract ArtifactGroup reduce(ArtifactGroup input)

    abstract Path report(JobName jobName, ArtifactGroup artifactGroup,
                              Double criteria, String fileName)

    abstract Path report(JobName jobName, MaterialList materialList,
                         String fileName)
}
