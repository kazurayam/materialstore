package com.kazurayam.materialstore.diff

import java.nio.file.Path

interface Differ {

    void setRoot(Path root)

    DiffArtifact makeDiff(DiffArtifact diffArtifact)

}
