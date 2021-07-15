package com.kazurayam.materials.store


import java.nio.file.Files
import java.nio.file.Path

class Jobber {

    private final JobName jobName
    private final JobTimestamp jobTimestamp
    private final Path jobResultDir

    private final Index index

    Jobber(Path root, JobName jobName, JobTimestamp jobTimestamp) {
        this.jobName = jobName
        this.jobTimestamp = jobTimestamp
        jobResultDir = root.resolve(jobName.toString()).resolve(jobTimestamp.toString())
        Files.createDirectories(getObjectsDir())

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

    Path getObjectsDir() {
        return getJobResultDir().resolve("objects")
    }

    /**
     * This "commit" method is the most significant operation in the TAOD project.
     *
     * @param metadata
     * @param data
     * @param fileType
     * @return Material
     */
    Material commit(Metadata metadata, byte[] data, FileType fileType) {
        Objects.requireNonNull(metadata)
        if (data.length == 0 ) throw new IllegalArgumentException("length of the data is 0")
        Objects.requireNonNull(fileType)

        MObject mObject = new MObject(data, fileType)

        // save the "byte[] data" into disk
        Path objectFile = this.getObjectsDir().resolve(mObject.getFileName())
        mObject.serialize(objectFile)

        // insert a line into the "index" content on memory
        index.put(mObject.getID(), fileType, metadata)

        // save the content of "index" into disk everytime when a commit is made
        index.serialize(Index.getIndexFile(jobResultDir))

        return new Material(mObject.getID(), mObject.getFileType(), metadata)
    }

    /**
     *
     * @param fileType
     * @param metadataPattern
     * @return
     */
    List<Material> select(FileType fileType, MetadataPattern metadataPattern) {
        Objects.requireNonNull(fileType)
        Objects.requireNonNull(metadataPattern)
        List<Material> result = new ArrayList<Material>()
        index.eachWithIndex { Material entry, x ->
            //ID id = entry.getID()
            //FileType ft = entry.getFileType()
            //Metadata md = entry.getMetadata()

        }
        throw new UnsupportedOperationException("TODO")
    }
}
