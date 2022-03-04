package com.kazurayam.materialstore

import com.kazurayam.materialstore.reduce.differ.DiffReporter
import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.MaterialList
import com.kazurayam.materialstore.reduce.MProductGroup
import com.kazurayam.materialstore.filesystem.Store

import java.nio.file.Path

abstract class Inspector {

    /**
     * the factory method
     */
    static final Inspector newInstance(Store store) {
        return new InspectorImpl(store)
    }

    abstract DiffReporter newReporter(JobName jobName)

    abstract MProductGroup reduce(MProductGroup input)

    abstract Path report(MProductGroup mProductGroup, Double criteria, String fileName)

    abstract Path report(MaterialList materialList, String fileName)
}
