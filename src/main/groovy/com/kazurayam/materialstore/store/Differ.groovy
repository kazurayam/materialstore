package com.kazurayam.materialstore.store

import com.kazurayam.materialstore.store.DiffArtifact

import java.nio.file.Path

interface Differ {

    void setRoot(Path root)

    DiffArtifact makeDiffArtifact(DiffArtifact diffArtifact)


}
