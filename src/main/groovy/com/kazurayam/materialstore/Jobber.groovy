package com.kazurayam.materialstore

import java.nio.file.Files
import java.nio.file.Path

final class Jobber {

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
    MaterialList selectMaterials(MetadataPattern metadataPattern, FileType fileType) {
        Objects.requireNonNull(metadataPattern)
        Objects.requireNonNull(fileType)
        MaterialList result = new MaterialList(jobTimestamp, metadataPattern, fileType)
        index.eachWithIndex { IndexEntry entry, x ->
            if (metadataPattern == MetadataPattern.ANY ||
                    entry.getMetadata().match(metadataPattern)) {
                if (fileType == FileType.NULL || fileType == entry.getFileType()) {
                    Material material = new Material(jobName, jobTimestamp, entry)
                    result.add(material)
                }
            }
        }
        return result
    }
    
    MaterialList selectMaterials(MetadataPattern metadataPattern) {
        return selectMaterials(metadataPattern, FileType.NULL)
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
     * This method writes the "byte[] data" into a File on disk.
     * The path of the file will be
     *     <root>/<JobName>/<JobTimestamp>/objects/<sha1 hash id>.<FileType.extension>
     *
     * And the "index" file will records MObjects; 1 line per 1 single MObject.
     * An entry of "index" will be like:
     *     <sha1 hash id>¥t<FileType.extension>¥t<Metadata>
     *
     * The "index" entries are identified uniquely by the combination of
     *     <FileType.extension> + <Metadata>
     *
     * You can not write (create) 2 or more MObjects (=index enteries) with the same
     * <FileType.extension> + <Metadata> combination.
     *
     * If you try to do write it, a MaterialstoreException will be raised.
     *
     * However, you can create 2 more more MObjects (=index entries)
     * with different key with the same `<sha1 hash id>.<FileType.extension>`.
     *
     * This means, you may possibly see such an index entries:
     *
     * <pre>
     * ac9be9a1053828f1e12e1cee4d66ff66adf60f9f	png	{"URL.file":"/", profile":"DevelopmentEnv"}
     * ac9be9a1053828f1e12e1cee4d66ff66adf60f9f	png	{"URL.file":"/", profile":"ProductionEnv"}
     * </pre>
     *
     * @param data
     * @param fileType
     * @param metadata
     * @return Material
     */
    Material write(byte[] data, FileType fileType, Metadata metadata)
            throws MaterialstoreException {
        Objects.requireNonNull(metadata)
        if (data.length == 0 ) throw new IllegalArgumentException("length of the data is 0")
        Objects.requireNonNull(fileType)

        if (index.containsKey(fileType, metadata)) {
            throw new MaterialstoreException("The combination of " +
                    "fileType=${fileType.getExtension()} and metadata=${metadata}" +
                    "is already there in the index")
        }

        MObject mObject = new MObject(data, fileType)

        // write the byte[] data into file if the MObject is not yet there.
        if (! mObject.exists(this.getObjectsDir())) {
            // save the "byte[] data" into disk
            Path objectFile = this.getObjectsDir().resolve(mObject.getFileName())
            mObject.serialize(objectFile)
        }

        // insert a line into the "index" content on memory
        IndexEntry indexEntry = index.put(mObject.getID(), fileType, metadata)

        // save the content of "index" into disk everytime when a commit is made
        index.serialize(Index.getIndexFile(jobResultDir))

        return new Material(this.getJobName(), this.getJobTimestamp(), indexEntry)
    }


}
