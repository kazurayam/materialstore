package com.kazurayam.materialstore.report

import com.kazurayam.materialstore.MaterialstoreException
import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.MaterialList
import com.kazurayam.materialstore.filesystem.Store

import java.nio.file.Path

abstract class MaterialListReporter {

    abstract Path report(MaterialList materialList, String reportFileName) throws MaterialstoreException

    static MaterialListReporter newInstance(Store store, JobName jobName) {
        return new MaterialListBasicReporter(store, jobName)
    }
}