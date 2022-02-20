package com.kazurayam.materialstore.differ

import com.kazurayam.materialstore.resolvent.DiffArtifact
import com.kazurayam.materialstore.resolvent.DiffArtifactGroup
import com.kazurayam.materialstore.resolvent.Resolvent
import com.kazurayam.materialstore.filesystem.FileType

interface DifferDriver extends Resolvent {

    DiffArtifactGroup differentiate(DiffArtifactGroup diffArtifactGroup)

    DiffArtifact differentiate(DiffArtifact diffArtifact)

    boolean hasDiffer(FileType fileType)
}