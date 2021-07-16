package com.kazurayam.materialstore.diff

import com.kazurayam.materialstore.store.JobName
import com.kazurayam.materialstore.store.JobTimestamp

import java.nio.file.Path

class DefaultReporter implements Reporter {

    DefaultReporter(Path root, JobName jobName, JobTimestamp jobTimestamp) {
        throw new UnsupportedOperationException("TODO")
    }

    void report(List<DiffArtifact> diffArtifacts, Path reportFile) {
        throw new UnsupportedOperationException("TODO")
    }

}
