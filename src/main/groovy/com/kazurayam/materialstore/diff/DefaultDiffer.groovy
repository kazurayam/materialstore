package com.kazurayam.materialstore.diff

import com.kazurayam.materialstore.store.JobName
import com.kazurayam.materialstore.store.JobTimestamp

import java.nio.file.Path

class DefaultDiffer implements Differ {

    private final Path root
    private final JobName jobName
    private final JobTimestamp jobTimestamp

    DefaultDiffer(Path root, JobName jobName, JobTimestamp jobTimestamp) {
        this.root = root
        this.jobName = jobName
        this.jobTimestamp = jobTimestamp
    }

    @Override
    List<DiffArtifact> makeDiff(List<DiffArtifact> dto) {
        throw new UnsupportedOperationException("TODO")
    }

}
