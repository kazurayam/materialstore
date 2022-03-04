package com.kazurayam.materialstore

import com.kazurayam.materialstore.reduce.differ.DiffReporter
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

    abstract DiffReporter newReporter(JobName jobName)

    abstract MProductGroup reduce(MProductGroup input)

    abstract Path report(MProductGroup mProductGroup, Double criteria, String fileName)

    abstract Path report(MaterialList materialList, String fileName)
}
