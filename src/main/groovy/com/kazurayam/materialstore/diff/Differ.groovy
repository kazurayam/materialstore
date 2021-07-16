package com.kazurayam.materialstore.diff

interface Differ {

    List<DiffArtifact> makeDiff(List<DiffArtifact> dto)

}
