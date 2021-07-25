package com.kazurayam.materialstore.store


import com.kazurayam.materialstore.store.FileType

interface DifferDriver {

    DiffArtifacts makeDiffArtifacts(DiffArtifacts diffArtifacts)

    boolean hasDiffer(FileType fileType)
}