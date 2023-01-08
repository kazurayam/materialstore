package com.kazurayam.materialstore.core.filesystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public final class Jobber {

    private static final Logger logger = LoggerFactory.getLogger(Jobber.class);
    private static final String OBJECTS_DIR_NAME = "objects";
    private final Store store;
    private final JobName jobName;
    private final JobTimestamp jobTimestamp;
    private final Path jobResultDir;
    private Index index;

    public enum DuplicationHandling {
        TERMINATE, CONTINUE
    }

    public Jobber(Store store, JobName jobName, JobTimestamp jobTimestamp) throws MaterialstoreException {
        Objects.requireNonNull(store);
        Objects.requireNonNull(jobName);
        Objects.requireNonNull(jobTimestamp);
        this.store = store;
        this.jobName = jobName;
        this.jobTimestamp = jobTimestamp;
        jobResultDir = store.getRoot().resolve(jobName.toString()).resolve(jobTimestamp.toString());
        try {
            Files.createDirectories(getObjectsDir());
        } catch (IOException e) {
            throw new MaterialstoreException(e);
        }
        index = new Index();
        // load content of the "index" file
        Path indexFile = Index.getIndexFile(jobResultDir);
        if (Files.exists(indexFile)) {
            index = Index.deserialize(indexFile);
        }

    }

    public Path getJobResultDir() {
        return jobResultDir;
    }

    public JobName getJobName() {
        return jobName;
    }

    public JobTimestamp getJobTimestamp() {
        return jobTimestamp;
    }

    public Path getObjectsDir() {
        return getJobResultDir().resolve(OBJECTS_DIR_NAME);
    }

    public byte[] read(final ID id, final IFileType fileType) throws MaterialstoreException {
        Objects.requireNonNull(id);
        Objects.requireNonNull(fileType);
        String fileName = id + "." + fileType.getExtension();
        Path objectFile = this.getObjectsDir().resolve(fileName);
        return MaterialIO.deserialize(objectFile);
    }

    public byte[] read(IndexEntry indexEntry) throws MaterialstoreException {
        Objects.requireNonNull(indexEntry);
        return this.read(indexEntry.getID(), indexEntry.getFileType());
    }

    public byte[] read(Material material) throws MaterialstoreException {
        Objects.requireNonNull(material);
        return this.read(material.getIndexEntry());
    }

    public MaterialList selectMaterials(final IFileType fileType, final QueryOnMetadata query) {
        Objects.requireNonNull(query);
        Objects.requireNonNull(fileType);
        final MaterialList result = new MaterialList(jobName, jobTimestamp, query);
        for (IndexEntry indexEntry : index) {
            if (query.equals(QueryOnMetadata.ANY) || query.matches(indexEntry.getMetadata())) {
                if (fileType.equals(FileType.NULL_OBJECT) || fileType.equals(indexEntry.getFileType())) {
                    Material material = new Material(store, getJobName(), getJobTimestamp(), indexEntry);
                    result.add(material);
                }
            }
        }
        return result;
    }

    public MaterialList selectMaterials(QueryOnMetadata query) {
        //
        return selectMaterials(FileType.NULL_OBJECT, query);
    }

    /*
     *
     */
    public Material selectMaterial(final ID id) {
        Objects.requireNonNull(id);
        for (IndexEntry indexEntry : index) {
            if (indexEntry.getMaterialIO().getID().equals(id)) {
                return new Material(store, jobName, jobTimestamp, indexEntry);
            }
        }
        return Material.NULL_OBJECT;
    }

    public int size() {
        return index.size();
    }

    /*
     * This method writes the "byte[] data" into a File on disk.
     * The path of the file will be
     * &lt;root&gt;/&lt;JobName&gt;/&lt;JobTimestamp&gt;/objects/&lt;sha1 hash id&gt;.&lt;FileType.extension&gt;
     * <p>
     * And the "index" file will record MaterialIO objects; 1 line per 1 single MaterialIO.
     * An entry of "index" will be like:
     * &lt;sha1 hash id&gt;¥t&lt;FileType.extension&gt;¥t&gt;Metadata&lt;
     * <p>
     * The "index" entries are identified uniquely by the combination of
     * &lt;FileType.extension&gt; + &lt;Metadata&gt;
     * <p>
     * You can not write (create) 2 or more IndexEntry objects with the same
     * &lt;FileType.extension&lt; + &lt;Metadata&gt; combination.
     * <p>
     * If you try to do write it, a MaterialstoreException will be raised.
     * <p>
     * However, you can create 2 or more IndexEntry objects
     * with different key with the same `&lt;sha1 hash id&gt;.&lt;FileType.extension&gt;`.
     * <p>
     * This means, you would possibly see such an index entries:
     *
     * <pre>
     * ac9be9a1053828f1e12e1cee4d66ff66adf60f9f	png	{"URL.file":"/", profile":"DevelopmentEnv"}
     * ac9be9a1053828f1e12e1cee4d66ff66adf60f9f	png	{"URL.file":"/", profile":"ProductionEnv"}
     * </pre>
     */
    public Material write(byte[] data, IFileType fileType, Metadata metadata) throws MaterialstoreException {
        return write(data, fileType, metadata, DuplicationHandling.TERMINATE);
    }

    public Material write(byte[] data, final IFileType fileType, final Metadata metadata,
                          final DuplicationHandling duplicationHandling)
            throws MaterialstoreException {
        Objects.requireNonNull(metadata);
        if (data.length == 0) throw new IllegalArgumentException("length of the data is 0");
        Objects.requireNonNull(fileType);

        if (index.containsKey(fileType, metadata)) {
            // the metadata has already been put in the index
            String msg1 = "The combination of " + "fileType=" + fileType.getExtension() + " and metadata=" + metadata + " is already there in the index";
            if (duplicationHandling.equals(DuplicationHandling.TERMINATE)) {
                // will stop the process entirely
                throw new DuplicatingMaterialException(msg1 + ".");

            } else if (duplicationHandling.equals(DuplicationHandling.CONTINUE)) {
                logger.info(msg1 + "; process skips one write and continue ...");
                // look up an entry of the index
                List<IndexEntry> indexEntries = index.indexEntriesOf(fileType, metadata);
                assert indexEntries.size() > 0;
                // return the Material
                return new Material(store, this.getJobName(), this.getJobTimestamp(), indexEntries.get(0));

            } else {
                throw new RuntimeException("Unsupported DuplicationHandling " + duplicationHandling);
            }

        } else {
            // new metadata should be stored in the directory
            // write the byte[] data into file if the MaterialIO is not yet there.
            ID id = new ID(MaterialIO.hashJDK(data));
            MaterialIO mio = new MaterialIO(id, fileType);
            if (!mio.existsInDir(this.getObjectsDir())) {
                // save the "byte[] data" into disk
                Path objectFile = this.getObjectsDir().resolve(mio.getFileName());
                MaterialIO.serialize(data, objectFile);
            }

            // insert a line into the "index" content on memory
            IndexEntry indexEntry = index.put(mio.getID(), fileType, metadata);
            // save the content of the "index" into a file on disk
            index.serialize(Index.getIndexFile(jobResultDir));
            return new Material(store, this.getJobName(), this.getJobTimestamp(), indexEntry);
        }

    }

    public static String getOBJECTS_DIR_NAME() {
        return OBJECTS_DIR_NAME;
    }

}
