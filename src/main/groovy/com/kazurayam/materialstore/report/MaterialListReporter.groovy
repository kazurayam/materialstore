package com.kazurayam.materialstore.report

import com.kazurayam.materialstore.MaterialstoreException
import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.MaterialList
import com.kazurayam.materialstore.filesystem.Store

import java.nio.file.Path

abstract class MaterialListReporter {

    abstract Path report(MaterialList materialList, String fileName) throws MaterialstoreException

    abstract void report(MaterialList materialList, Path filePath) throws MaterialstoreException

    static final MaterialListReporter newInstance(Store store, JobName jobName) {
        return new MaterialListBasicReporter(store, jobName)
    }

    static final String getTitle(Path file) {
        String fileName = file.getFileName().toString()
        return fileName.substring(0, fileName.indexOf(".html"))
    }

}