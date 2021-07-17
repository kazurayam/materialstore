package com.kazurayam.materialstore.diff

import com.kazurayam.materialstore.report.DiffReporter
import com.kazurayam.materialstore.store.JobName
import com.kazurayam.materialstore.store.JobTimestamp

import java.nio.file.Path

class DefaultReporter implements DiffReporter {

    DefaultReporter(Path root, JobName jobName, JobTimestamp jobTimestamp) {
        throw new UnsupportedOperationException("TODO")
    }

    void reportDiffs(List<DiffArtifact> diffArtifacts, Path reportFile) {
        throw new UnsupportedOperationException("TODO")
    }

}
