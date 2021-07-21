package com.kazurayam.materialstore.store

import com.kazurayam.materialstore.store.DiffArtifact

import java.nio.file.Path

interface DiffReporter {

    static DiffReporter 
    void reportDiffs(List<DiffArtifact> diffArtifacts, Path reportFile)

}
