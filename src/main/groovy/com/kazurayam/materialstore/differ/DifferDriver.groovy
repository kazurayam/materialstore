package com.kazurayam.materialstore.differ

import com.kazurayam.materialstore.diffartifact.DiffArtifact
import com.kazurayam.materialstore.diffartifact.Resolvent
import com.kazurayam.materialstore.filesystem.FileType

interface DifferDriver extends Resolvent {

    List<DiffArtifact> differentiate(List<DiffArtifact> diffArtifactList)

    DiffArtifact differentiate(DiffArtifact diffArtifact)

    boolean hasDiffer(FileType fileType)
}