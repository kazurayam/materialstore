package com.kazurayam.materialstore.store

import com.kazurayam.materialstore.store.DiffArtifact
import com.kazurayam.materialstore.store.FileType

interface DifferDriver {

    List<DiffArtifact> makeDiffArtifacts(List<DiffArtifact> diffArtifacts)

    boolean hasDiffer(FileType fileType)
}