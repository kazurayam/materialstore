package com.kazurayam.materialstore.report

import com.kazurayam.materialstore.diff.DiffArtifact

import java.nio.file.Path

interface DiffReporter {

    void reportDiffs(List<DiffArtifact> diffArtifacts, Path reportFile)

}
