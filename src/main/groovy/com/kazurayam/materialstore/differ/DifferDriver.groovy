package com.kazurayam.materialstore.differ

import com.kazurayam.materialstore.diffartifact.DiffArtifact
import com.kazurayam.materialstore.filesystem.FileType
import com.kazurayam.materialstore.diffartifact.DiffArtifactGroup

interface DifferDriver {

    DiffArtifact differentiate(DiffArtifact diffArtifact)

    List<DiffArtifact> differentiate(List<DiffArtifact> diffArtifactList)

    boolean hasDiffer(FileType fileType)
}