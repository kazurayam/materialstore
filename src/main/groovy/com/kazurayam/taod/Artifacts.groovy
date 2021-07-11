package com.kazurayam.taod

import java.nio.file.Files
import java.nio.file.Path

class Artifacts {

    private final Path root_
    private final JobName jobName_
    private final JobTimestamp jobTimestamp_
    private final Path artifactsDir_
    private final Index index_
    private final Set<Blob> blobs_

    Artifacts(Path root, JobName jobName, JobTimestamp jobTimestamp) {
        root_ = root
        jobName_ = jobName
        jobTimestamp_ = jobTimestamp
        artifactsDir_ = root_.resolve(jobName_.toString()).resolve(jobTimestamp_.toString())
        Files.createDirectories(getBlobsDir())
        index_ = new Index(artifactsDir_)
        List<ID> listIDs = index_.listIDs()
        blobs_ = new HashSet<Blob>()
        for (ID id in listIDs) {
            Blob blob = Blob.deserialize(getBlobsDir(), id)
            blobs_.add(blob)
        }
    }

    /**
     *
     * @param metadata
     * @param data
     * @return
     */
    ID commit(Metadata metadata, byte[] data) {
        Blob blob = new Blob(data)
        blob.serialize(getBlobsDir())
        index_.put(metadata, blob.getID())
        index_.serialize()
        return blob.getID()
    }

    Path getBlobsDir() {
        return artifactsDir_.resolve("blobs")
    }
}
