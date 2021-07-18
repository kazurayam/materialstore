package com.kazurayam.materialstore.diff

import com.kazurayam.materialstore.store.FileType

import java.nio.file.Path

interface DifferDriver {

    List<DiffArtifact> makeDiff(List<DiffArtifact> diffArtifacts)

    boolean hasDiffer(FileType fileType)
}