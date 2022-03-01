package com.kazurayam.materialstore

import com.kazurayam.materialstore.differ.DiffReporter
import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.MaterialList
import com.kazurayam.materialstore.reduce.MProductGroup
import com.kazurayam.materialstore.reduce.Reducer
import com.kazurayam.materialstore.filesystem.Store

import java.nio.file.Path

abstract class MaterialstoreFacade {

    /**
     * the factory method
     */
    static final MaterialstoreFacade newInstance(Store store) {
        return new MaterialstoreFacadeImpl(store)
    }

    abstract void addResolvent(Reducer resolvent)

    abstract Path getRoot()

    abstract Store getStore()

    abstract DiffReporter newReporter(JobName jobName)

    abstract MProductGroup reduce(MProductGroup input)

    abstract Path report(JobName jobName, MProductGroup mProductGroup,
                         Double criteria, String fileName)

    abstract Path report(JobName jobName, MaterialList materialList,
                         String fileName)
}
