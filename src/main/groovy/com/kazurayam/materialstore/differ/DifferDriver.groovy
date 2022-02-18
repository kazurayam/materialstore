package com.kazurayam.materialstore.differ


import com.kazurayam.materialstore.filesystem.FileType
import com.kazurayam.materialstore.diffartifact.DiffArtifactGroup

interface DifferDriver {

    DiffArtifactGroup differentiate(DiffArtifactGroup diffArtifactgroup)

    boolean hasDiffer(FileType fileType)
}