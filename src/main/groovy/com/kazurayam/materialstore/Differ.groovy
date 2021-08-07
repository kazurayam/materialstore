package com.kazurayam.materialstore


import java.nio.file.Path

interface Differ {

    void setRoot(Path root)

    DiffArtifact makeDiffArtifact(DiffArtifact diffArtifact)


}
