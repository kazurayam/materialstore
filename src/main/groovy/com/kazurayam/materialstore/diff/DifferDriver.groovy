package com.kazurayam.materialstore.diff

import java.nio.file.Path

interface DifferDriver {

    void setRoot(Path root)

    List<DiffArtifact> makeDiff(List<DiffArtifact> diffArtifacts)

}