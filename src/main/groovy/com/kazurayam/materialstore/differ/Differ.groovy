package com.kazurayam.materialstore.differ

import com.kazurayam.materialstore.diffartifact.DiffArtifact

import java.nio.file.Path

interface Differ {

    DiffArtifact makeDiffArtifact(DiffArtifact diffArtifact)

    void setRoot(Path root)

}
