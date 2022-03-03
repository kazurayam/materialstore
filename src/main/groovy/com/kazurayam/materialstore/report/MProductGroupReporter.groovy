package com.kazurayam.materialstore.report

import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.Store
import com.kazurayam.materialstore.reduce.MProductGroup

import java.nio.file.Path

abstract class MProductGroupReporter {

    abstract Path report(MProductGroup mProductGroup, String fileName)

    static MProductGroupReporter newInstance(Store store, JobName jobName) {
        return new MProductGroupBasicReporter(store, jobName)
    }
}
