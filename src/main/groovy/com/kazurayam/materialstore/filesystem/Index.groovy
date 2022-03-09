package com.kazurayam.materialstore.filesystem

import com.kazurayam.materialstore.MaterialstoreException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors

/**
 * The "index" file is a list of files contained in the "Job" directory.
 * The line contains a set of metadata about each file.
 *
 * Every lines of "index" is in the format as follows
 * 1. ID as 40 HexDecimal characters
 * 2. seperated by a TAB
 * 3. FileType such as "png" = filename extension
 * 4. seperated by a TAB
 * 5. Metadata in List literal: "data" seperated by a comma, enclosed by [ and ]
 *
 * e.g,
 * 6141b40cfe9e7340a483a3097c4f6ff5d20e04ea\tpng\t{"URL":"http://demoaut-mimic.kazurayam.com/","profile":"DevelopmentEnv"}
 */
final class Index {

    private static final Logger logger_ = LoggerFactory.getLogger(Index.class)

    private final List<IndexEntry> lines_
    // Tuple of (ID, FileType, Metadata)

    Index() {
        lines_ = new ArrayList<IndexEntry>()
    }

    static Path getIndexFile(Path jobDir) {
        return jobDir.resolve("index")
    }

    List<IndexEntry> indexEntriesOf(FileType fileType, Metadata metadata) {
        List<IndexEntry> filtered =
                lines_.stream()
                        .filter { IndexEntry ie ->
                            ie.getFileType() == fileType &&
                                    ie.getMetadata() == metadata
                        }
                        .collect(Collectors.toList())
        return filtered
    }

    boolean containsKey(FileType fileType, Metadata metadata) {
        List<IndexEntry> filtered = this.indexEntriesOf(fileType, metadata)
        return filtered.size() > 0
    }

    IndexEntry put(ID id, FileType fileType, Metadata metadata) {
        if (this.containsKey(fileType, metadata)) {
            throw new MaterialstoreException("the combination of " +
                    "fileType:${fileType.getExtension()} and " +
                    "metadata:${metadata.toString()} is already " +
                    "there in the index")
        }
        IndexEntry indexEntry = new IndexEntry(new MObject(id, fileType), metadata)
        lines_.add(indexEntry)
        return indexEntry
    }

    int size() {
        return lines_.size()
    }

    Iterator<IndexEntry> iterator() {
        return lines_.iterator()
    }

    /**
     * write data int "index" file.
     * lines are sorted by the order of Metadata > FileType > ID
     * @param indexFile
     */
    void serialize(Path indexFile) {
        FileOutputStream fos = new FileOutputStream(indexFile.toFile())
        OutputStreamWriter osw = new OutputStreamWriter(fos, "utf-8")
        BufferedWriter br = new BufferedWriter(osw)
        List<IndexEntry> sorted = lines_.stream().sorted().collect(Collectors.toList())
        sorted.each { IndexEntry indexEntry ->
            String s = formatLine(indexEntry)
            br.println(s)
        }
        br.flush()
        br.close()
    }

    static String formatLine(IndexEntry indexEntry) {
        Objects.requireNonNull(indexEntry)
        ID id = indexEntry.getID()
        FileType ft = indexEntry.getFileType()
        Metadata md = indexEntry.getMetadata()
        StringBuilder sb = new StringBuilder()
        sb.append(id.toString())
        sb.append("\t")
        sb.append(ft.getExtension())
        sb.append("\t")
        sb.append(md.toString())
        return sb.toString()
    }

    /**
     * read the "index" file
     *
     * @param file
     * @return
     */
    static Index deserialize(Path indexFile) {
        Objects.requireNonNull(indexFile)
        if (! Files.exists(indexFile)) {
            throw new IllegalArgumentException("${indexFile} is not found")
        }
        Index index = new Index()
        //
        File file = indexFile.toFile()
        String line
        int x = 0
        file.withReader { reader ->
            while ((line = reader.readLine()) != null) {
                x += 1
                try {
                    IndexEntry indexEntry = IndexEntry.parseLine(line)
                    if (indexEntry != null) {
                        index.put(
                                indexEntry.getID(),
                                indexEntry.getFileType(),
                                indexEntry.getMetadata())
                    }
                } catch (IllegalArgumentException e) {
                    logger_.warn("LINE#=${x} \'${line}\' ${e.getMessage()}")
                }
            }
        }
        return index
    }

    @Override
    String toString() {
        StringBuilder sb = new StringBuilder()
        sb.append("{")
        sb.append("\"indexEntries\": [")
        int count = 0
        lines_.each { entry ->
            if (count > 0) {
                sb.append(",")
            }
            sb.append(entry.toString())
            count += 1
        }
        sb.append("]")
        sb.append("}")
        return sb.toString()
    }
}
