package com.kazurayam.materialstore.differ

import com.kazurayam.materialstore.diffartifact.DiffArtifact

import java.nio.file.Path

interface Differ {

    void setRoot(Path root)

    DiffArtifact makeDiffArtifact(DiffArtifact diffArtifact)


}
