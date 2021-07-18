package com.kazurayam.materialstore.diff

import com.kazurayam.materialstore.store.FileType

interface DifferDriver {

    List<DiffArtifact> makeDiff(List<DiffArtifact> diffArtifacts)

    boolean hasDiffer(FileType fileType)
}