package com.kazurayam.materialstore.store


import com.kazurayam.materialstore.MaterialstoreException

import java.nio.file.Files
import java.nio.file.Path

class Jobber {

    static final String OBJECTS_DIR_NAME = "objects"

    private final JobName jobName
    private final JobTimestamp jobTimestamp
    private final Path jobResultDir

    private Index index

    Jobber(Path root, JobName jobName, JobTimestamp jobTimestamp) {
        Objects.requireNonNull(root)
        Objects.requireNonNull(jobName)
        Objects.requireNonNull(jobTimestamp)

        this.jobName = jobName
        this.jobTimestamp = jobTimestamp
        jobResultDir = root.resolve(jobName.toString()).resolve(jobTimestamp.toString())
        Files.createDirectories(getObjectsDir())

        index = new Index()
        // load content of the "index" file
        Path indexFile = Index.getIndexFile(jobResultDir)
        if (Files.exists(indexFile)) {
            index = Index.deserialize(indexFile)
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
        return getJobResultDir().resolve(OBJECTS_DIR_NAME)
    }

    /**
     *
     * @param material
     * @return
     */
    byte[] read(ID id, FileType fileType) {
        Objects.requireNonNull(id)
        Objects.requireNonNull(fileType)
        String fileName = "${id.toString()}.${fileType.getExtension()}"
        Path objectFile = this.getObjectsDir().resolve(fileName)
        MObject mObject = MObject.deserialize(objectFile, fileType)
        return mObject.getData()
    }

    byte[] read(IndexEntry indexEntry) {
        Objects.requireNonNull(indexEntry)
        return this.read(indexEntry.getID(), indexEntry.getFileType())
    }

    byte[] read(Material material) {
        Objects.requireNonNull(material)
        return this.read(material.getIndexEntry())
    }

    /**
     *
     * @param fileType
     * @param metadataPattern
     * @return
     */
    List<Material> selectMaterials(FileType fileType, MetadataPattern metadataPattern) {
        Objects.requireNonNull(fileType)
        Objects.requireNonNull(metadataPattern)
        List<Material> result = new ArrayList<Material>()
        index.eachWithIndex { IndexEntry entry, x ->
            if (entry.getFileType() == fileType &&
                    entry.getMetadata().match(metadataPattern)) {
                Material material = new Material(jobName, jobTimestamp, entry)
                result.add(material)
            }
        }
        return result
    }

    /**
     *
     * @param id
     * @return
     */
    Material selectMaterial(ID id) {
        Objects.requireNonNull(id)
        Material result = null
        index.eachWithIndex { IndexEntry entry, x ->
            if (entry.getID() == id) {
                result = new Material(jobName, jobTimestamp, entry)
                return
            }
        }
        return result
    }

    int size() {
        return index.size()
    }

    /**
     * This "commit" method is the most significant operation in the TAOD project.
     *
     * Each MObject is identified by its FileType + Metadata combination.
     * Jobber ensures that MObject under the "object" is unique by FileType + Metadata.
     * If you try to commit a MObject with duplicating FileType + Metadata, the commit
     * will be rejected.
     *
     * @param metadata
     * @param data
     * @param fileType
     * @return Material
     */
    Material write(byte[] data, FileType fileType, Metadata metadata)
            throws MaterialstoreException {
        Objects.requireNonNull(metadata)
        if (data.length == 0 ) throw new IllegalArgumentException("length of the data is 0")
        Objects.requireNonNull(fileType)

        MObject mObject = new MObject(data, fileType)

        // check if the MObject is already there.
        if (mObject.exists(this.getObjectsDir())) {
            throw new MaterialstoreException("fileType=${fileType} metadata=${metadata}:" +
                    " MObject is already in the Store." +
                    " Metadata is duplicating." +
                    " Give more detailed metadata to make this object uniquely identifiable.")
        }

        // save the "byte[] data" into disk
        Path objectFile = this.getObjectsDir().resolve(mObject.getFileName())
        mObject.serialize(objectFile)

        // insert a line into the "index" content on memory
        IndexEntry indexEntry = index.put(mObject.getID(), fileType, metadata)

        // save the content of "index" into disk everytime when a commit is made
        index.serialize(Index.getIndexFile(jobResultDir))

        return new Material(this.getJobName(), this.getJobTimestamp(), indexEntry)
    }


}
