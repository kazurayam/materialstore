package com.kazurayam.materialstore.store

import com.kazurayam.materialstore.store.DiffArtifact

interface DiffReporter {

    void reportDiffs(List<DiffArtifact> diffArtifacts, String reportFileName)

}
