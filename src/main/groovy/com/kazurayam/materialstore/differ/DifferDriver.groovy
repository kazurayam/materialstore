package com.kazurayam.materialstore.differ


import com.kazurayam.materialstore.resolvent.Artifact
import com.kazurayam.materialstore.resolvent.ArtifactGroup
import com.kazurayam.materialstore.resolvent.Resolvent
import com.kazurayam.materialstore.filesystem.FileType

interface DifferDriver extends Resolvent {

    ArtifactGroup differentiate(ArtifactGroup artifactGroup)

    Artifact differentiate(Artifact artifact)

    boolean hasDiffer(FileType fileType)
}