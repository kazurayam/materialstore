package com.kazurayam.taod

import groovy.json.JsonOutput

import java.nio.file.Files
import java.nio.file.Path

class JobResult implements Comparable {

    private final JobName jobName
    private final JobTimestamp jobTimestamp
    private final Path jobResultDir

    private final Index index

    JobResult(Path root, JobName jobName, JobTimestamp jobTimestamp) {
        this.jobName = jobName
        this.jobTimestamp = jobTimestamp
        jobResultDir = root.resolve(jobName.toString()).resolve(jobTimestamp.toString())
        Files.createDirectories(getArtifactsDir())

        // the content of "index" is cached in memory
        index = new Index()
        Path indexFile = Index.getIndexFile(jobResultDir)
        if (Files.exists(indexFile)) {
            index.deserialize(indexFile)
        }
    }

    Path getJobResultDir() {
        return jobResultDir
    }

    JobName getJobName() {
        return jobName
    }

    JobTimestamp getJobTimestamp() {
        return jobTimestamp
    }

    Path getArtifactsDir() {
        return getJobResultDir().resolve("artifacts")
    }

    /**
     * This "commit" method is the most significant one of the TAOD project.
     *
     * @param metadata
     * @param data
     * @return
     */
    ID commit(Metadata metadata, byte[] data, FileType fileType) {
        Artifact artifact = new Artifact(data, fileType)

        // save the "byte[] data" into disk
        Path artifactFile = this.getArtifactsDir().resolve(artifact.getFileName())
        artifact.serialize(artifactFile)

        // insert a line into the "index" content on memory
        index.put(artifact.getID(), fileType, metadata)

        // save the content of "index" into disk everytime when a commit is made
        index.serialize(Index.getIndexFile(jobResultDir))

        return artifact.getID()
    }

    @Override
    boolean equals(Object obj) {
        if (! obj instanceof JobResult) {
            return false
        }
        JobResult other = (JobResult)obj
        return other.getJobResultDir() == this.getJobResultDir()
    }

    @Override
    int hashCode() {
        return this.getJobResultDir().hashCode()
    }

    @Override
    int compareTo(Object obj) {
        if (! obj instanceof JobResult) {
            throw new IllegalArgumentException("obj is not instance of Job")
        }
        JobResult other = (JobResult)obj
        return this.getJobResultDir() <=> other.getJobResultDir()
    }

    @Override
    String toString() {
        Map m = ["jobName": this.jobName.toString(),
                 "jobTimestamp": this.jobTimestamp.toString(),
                 "jobResultDir": this.jobResultDir.toString()]
        String json = JsonOutput.toJson(m)
        return json
    }
}
