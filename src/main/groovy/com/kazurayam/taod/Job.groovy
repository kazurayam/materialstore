package com.kazurayam.taod

import java.nio.file.Files
import java.nio.file.Path

class Job {

    private final JobName jobName_
    private final JobTimestamp jobTimestamp_
    private final Path jobDir_
    private final Index index_

    Job(Path root, JobName jobName, JobTimestamp jobTimestamp) {
        jobName_ = jobName
        jobTimestamp_ = jobTimestamp
        jobDir_ = root.resolve(jobName.toString()).resolve(jobTimestamp.toString())
        Files.createDirectories(getArtifactsDir())
        index_ = new Index()
        Path indexFile = Index.getIndexFile(jobDir_)
        if (Files.exists(indexFile)) {
            index_.deserialize(indexFile)
        }
    }

    JobName getJobName() {
        return jobName_
    }

    JobTimestamp getJobTimestamp() {
        return jobTimestamp_
    }

    /**
     *
     * @param metadata
     * @param data
     * @return
     */
    ID commit(Metadata metadata, byte[] data, FileType fileType) {
        //
        Artifact artifact = new Artifact(data, fileType)
        artifact.serialize(getArtifactsDir())
        //
        index_.put(artifact.getID(), fileType, metadata)
        index_.serialize(Index.getIndexFile(jobDir_))
        return artifact.getID()
    }

    Path getArtifactsDir() {
        return jobDir_.resolve("artifacts")
    }
}
