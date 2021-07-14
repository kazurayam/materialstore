package com.kazurayam.materials.diff

interface Differ {

    List<DiffArtifact> process(List<DiffArtifact> dto)

}
