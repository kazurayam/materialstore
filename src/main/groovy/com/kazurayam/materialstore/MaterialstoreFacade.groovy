package com.kazurayam.materialstore

import com.kazurayam.materialstore.differ.DiffReporter
import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.MaterialList
import com.kazurayam.materialstore.resolvent.ArtifactGroup
import com.kazurayam.materialstore.resolvent.Resolvent
import com.kazurayam.materialstore.filesystem.Store

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

    abstract Path reportArtifactGroup(JobName jobName, ArtifactGroup artifactGroup,
                              Double criteria, String fileName)

    abstract Path reportMaterials(JobName jobName, MaterialList materialList,
                         String fileName = "list.html")

    abstract ArtifactGroup workOn(ArtifactGroup input)

}
