package com.kazurayam.materialstore.report

import com.kazurayam.materialstore.MaterialstoreException
import com.kazurayam.materialstore.filesystem.MaterialList

import java.nio.file.Path

abstract class MaterialListReporter extends AbstractReporter {

    abstract Path report(MaterialList materialList, String fileName) throws MaterialstoreException

    abstract void report(MaterialList materialList, Path filePath) throws MaterialstoreException

}