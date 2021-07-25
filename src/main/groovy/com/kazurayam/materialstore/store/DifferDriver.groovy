package com.kazurayam.materialstore.store


import com.kazurayam.materialstore.store.FileType

interface DifferDriver {

    DiffArtifacts differentiate(DiffArtifacts diffArtifacts)

    boolean hasDiffer(FileType fileType)
}