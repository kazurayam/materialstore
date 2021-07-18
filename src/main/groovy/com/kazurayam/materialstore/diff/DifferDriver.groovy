package com.kazurayam.materialstore.diff

import java.nio.file.Path

interface DifferDriver {

    List<DiffArtifact> makeDiff(List<DiffArtifact> diffArtifacts)

}