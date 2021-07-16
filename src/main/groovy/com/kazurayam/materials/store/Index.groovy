package com.kazurayam.materials.store

import groovy.json.JsonOutput
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
class Index implements Comparable {

    private static final Logger logger_ = LoggerFactory.getLogger(Index.class)

    private final Path indexFile_
    private final List<IndexEntry> lines_
    // Tuple of (ID, FileType, Metadata)

    Index() {
        lines_ = new ArrayList<IndexEntry>()
    }

    static Path getIndexFile(Path jobDir) {
        return jobDir.resolve("index")
    }

    IndexEntry put(ID id, FileType fileType, Metadata metadata) {
        IndexEntry indexEntry = new IndexEntry(id, fileType, metadata)
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
    boolean equals(Object obj) {
        if (! obj instanceof Index) {
            return false
        }
        Index other = (Index)obj
        return indexFile_ == indexFile_ && lines_ == lines_
    }

    @Override
    int hashCode() {
        indexFile_.hashCode()
    }

    @Override
    String toString() {
        Map m = ["indexFile": indexFile_, "indexEntries": lines_]
        return new JsonOutput().toJson(m)
    }

    @Override
    int compareTo(Object obj) {
        if (! obj instanceof Index) {
            throw new IllegalArgumentException("obj is not instance of Index")
        }
        Index other = (Index)obj
        return this.indexFile_ <=> other.indexFile_
    }
}
