package com.kazurayam.materialstore.diff

import com.kazurayam.materialstore.diff.DiffArtifact

import java.nio.file.Path

interface DiffReporter {

    static DiffReporter 
    void reportDiffs(List<DiffArtifact> diffArtifacts, Path reportFile)

}
