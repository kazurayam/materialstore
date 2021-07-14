package com.kazurayam.materials.diff

import java.nio.file.Path

interface Reporter {

    void report(List<DiffArtifact> diffArtifacts, Path reportFile)

}
