package com.kazurayam.materialstore.filesystem


import com.kazurayam.materialstore.MaterialstoreException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Files
import java.nio.file.Path

final class Jobber {

    private static final Logger logger_ = LoggerFactory.getLogger(Jobber.class)

    static final String OBJECTS_DIR_NAME = "objects"

    static enum DuplicationHandling {
        TERMINATE,
        CONTINUE,
    }

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
        return MaterialIO.deserialize(objectFile)
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
     * @param query
     * @return
     */
    MaterialList selectMaterials(QueryOnMetadata query, FileType fileType) {
        Objects.requireNonNull(query)
        Objects.requireNonNull(fileType)
        MaterialList result = new MaterialList(jobName, jobTimestamp, query)
        index.eachWithIndex { IndexEntry entry, x ->
            if (query == QueryOnMetadata.ANY ||
                    query.matches(entry.getMetadata())) {
                if (fileType == FileType.NULL_OBJECT || fileType == entry.getFileType()) {
                    Material material = new Material(jobName, jobTimestamp, entry)
                    result.add(material)
                }
            }
        }
        return result
    }
    
    MaterialList selectMaterials(QueryOnMetadata query) {
        return selectMaterials(query, FileType.NULL_OBJECT)
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
            if (entry.getMaterialIO().getID() == id) {
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
     * And the "index" file will records MaterialIO objects; 1 line per 1 single MaterialIO.
     * An entry of "index" will be like:
     *     <sha1 hash id>¥t<FileType.extension>¥t<Metadata>
     *
     * The "index" entries are identified uniquely by the combination of
     *     <FileType.extension> + <Metadata>
     *
     * You can not write (create) 2 or more IndexEntry objects with the same
     * <FileType.extension> + <Metadata> combination.
     *
     * If you try to do write it, a MaterialstoreException will be raised.
     *
     * However, you can create 2 or more IndexEntry objects
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
     * @param duplicationHandling
     * @return Material
     */
    Material write(byte[] data, FileType fileType, Metadata metadata) {
        write(data, fileType, metadata, DuplicationHandling.TERMINATE)
    }

    Material write(byte[] data, FileType fileType, Metadata metadata,
                   DuplicationHandling duplicationHandling) throws MaterialstoreException {
        Objects.requireNonNull(metadata)
        if (data.length == 0 ) throw new IllegalArgumentException("length of the data is 0")
        Objects.requireNonNull(fileType)

        if (index.containsKey(fileType, metadata)) {
            // the metadata has already been put in the index
            String msg1 = "The combination of " +
                    "fileType=${fileType.getExtension()} and metadata=${metadata}" +
                    " is already there in the index"
            if (duplicationHandling == DuplicationHandling.TERMINATE) {
                // will stop the process entirely
                throw new MaterialstoreException(msg1 + ".")

            } else if (duplicationHandling == DuplicationHandling.CONTINUE) {
                logger_.info(msg1 + "; process skips one write and continue ...")
                // look up an entry of the index
                List<IndexEntry> indexEntries = index.indexEntriesOf(fileType, metadata)
                assert indexEntries.size() > 0
                // return the Material
                return new Material(this.getJobName(), this.getJobTimestamp(), indexEntries.get(0))

            } else {
                throw new RuntimeException("Unsupported DuplicationHandling ${duplicationHandling}")
            }
        } else {
            // new metadata should be stored in the directory
            // write the byte[] data into file if the MaterialIO is not yet there.
            ID id = new ID(MaterialIO.hashJDK(data))
            MaterialIO mio = new MaterialIO(id, fileType)
            if (!mio.existsInDir(this.getObjectsDir())) {
                // save the "byte[] data" into disk
                Path objectFile = this.getObjectsDir().resolve(mio.getFileName())
                mio.serialize(data, objectFile)
            }
            // insert a line into the "index" content on memory
            IndexEntry indexEntry = index.put(mio.getID(), fileType, metadata)
            // save the content of the "index" into a file on disk
            index.serialize(Index.getIndexFile(jobResultDir))
            return new Material(this.getJobName(), this.getJobTimestamp(), indexEntry)
        }
    }

}
