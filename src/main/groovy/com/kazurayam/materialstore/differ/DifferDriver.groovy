package com.kazurayam.materialstore.differ

import com.kazurayam.materialstore.diffartifact.DiffArtifact
import com.kazurayam.materialstore.diffartifact.DiffArtifactGroup
import com.kazurayam.materialstore.diffartifact.Resolvent
import com.kazurayam.materialstore.filesystem.FileType

interface DifferDriver extends Resolvent {

    DiffArtifactGroup differentiate(DiffArtifactGroup diffArtifactGroup)

    DiffArtifact differentiate(DiffArtifact diffArtifact)

    boolean hasDiffer(FileType fileType)
}