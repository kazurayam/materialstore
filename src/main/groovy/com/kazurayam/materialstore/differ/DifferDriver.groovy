package com.kazurayam.materialstore.differ

import com.kazurayam.materialstore.filesystem.FileType
import com.kazurayam.materialstore.diffartifact.DiffArtifacts

interface DifferDriver {

    DiffArtifacts differentiate(DiffArtifacts diffArtifacts)

    boolean hasDiffer(FileType fileType)
}