package com.kazurayam.taod

import groovy.json.JsonOutput

import java.nio.file.Files
import java.nio.file.Path

class Job implements Comparable {

    private final JobName jobName
    private final JobTimestamp jobTimestamp
    private final Path jobDir

    private final Index index

    Job(Path root, JobName jobName, JobTimestamp jobTimestamp) {
        this.jobName = jobName
        this.jobTimestamp = jobTimestamp
        jobDir = root.resolve(jobName.toString()).resolve(jobTimestamp.toString())
        Files.createDirectories(getArtifactsDir())

        // the content of "index" is cached in memory
        index = new Index()
        Path indexFile = Index.getIndexFile(jobDir)
        if (Files.exists(indexFile)) {
            index.deserialize(indexFile)
        }
    }

    Path getJobDir() {
        return jobDir
    }

    JobName getJobName() {
        return jobName
    }

    JobTimestamp getJobTimestamp() {
        return jobTimestamp
    }

    /**
     * This "commit" method is the most significant one of the TAOD project.
     *
     * @param metadata
     * @param data
     * @return
     */
    ID commit(Metadata metadata, byte[] data, FileType fileType) {
        //
        Artifact artifact = new Artifact(data, fileType)
        // save the "byte[] data" into disk
        artifact.serialize(getArtifactsDir())

        // insert a line into the "index" content on memory
        index.put(artifact.getID(), fileType, metadata)

        // save the content of "index" into disk everytime when a commit is made
        index.serialize(Index.getIndexFile(jobDir))

        return artifact.getID()
    }

    Path getArtifactsDir() {
        return jobDir.resolve("artifacts")
    }

    @Override
    boolean equals(Object obj) {
        if (! obj instanceof Job) {
            return false
        }
        Job other = (Job)obj
        return other.getJobDir() == this.getJobDir()
    }

    @Override
    int hashCode() {
        return this.getJobDir().hashCode()
    }

    @Override
    int compareTo(Object obj) {
        if (! obj instanceof Job) {
            throw new IllegalArgumentException("obj is not instance of Job")
        }
        Job other = (Job)obj
        return this.getJobDir() <=> other.getJobDir()
    }

    @Override
    String toString() {
        Map m = ["jobName": this.jobName.toString(), "jobTimestamp": this.jobTimestamp.toString(), "jobDir": this.jobDir.toString()]
        String json = JsonOutput.toJson(m)
        return json
    }
}
