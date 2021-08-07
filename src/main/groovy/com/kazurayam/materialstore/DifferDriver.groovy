package com.kazurayam.materialstore

interface DifferDriver {

    DiffArtifacts differentiate(DiffArtifacts diffArtifacts)

    boolean hasDiffer(FileType fileType)
}