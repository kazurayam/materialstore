package com.kazurayam.materialstore


import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.MaterialList
import com.kazurayam.materialstore.reduce.MProductGroup
import com.kazurayam.materialstore.filesystem.Store
import com.kazurayam.materialstore.report.MProductGroupReporter

import java.nio.file.Path

abstract class Inspector {

    /**
     * the factory method
     */
    static final Inspector newInstance(Store store) {
        return new InspectorImpl(store)
    }

    abstract MProductGroupReporter newReporter(JobName jobName)

    abstract MProductGroup reduce(MProductGroup input)

    abstract Path report(MProductGroup mProductGroup, Double criteria, String fileName)

    abstract void report(MProductGroup mProductGroup, Double criteria, Path filePath)

    abstract Path report(MaterialList materialList, String fileName)

    abstract void report(MaterialList materialList, Path filePath)
}
