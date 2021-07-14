package com.kazurayam.materials.diff

interface Differ {

    List<DiffArtifact> makeDiff(List<DiffArtifact> dto)

}
