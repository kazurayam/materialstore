package com.kazurayam.taod

import java.nio.file.Files
import java.nio.file.Path

class Index implements Comparable {

    private final Path file_

    private Map<Metadata, ID> index_ = null

    private Index() { throw new UnsupportedOperationException() }

    Index(Path jobDir) {
        file_ = jobDir.resolve("index")
        if (Files.exists(file_)) {
            index_ = loadFile(file_)
        } else {
            index_ = new HashMap<Metadata, ID>()
        }
    }

    /**
     *
     * @return
     */
    List<ID> listIDs() {
        return index_.values().toSorted()
    }

    /**
     * read the "index" file, which is in the format like
     * ```
     * <Artifact ID>\t<Artifact Metadata[0]>\t<Metadata[1]>\t<Metadata[2]>...
     * ```
     * @param file
     * @return
     */
    static Map<Metadata, ID> loadFile(Path filePath) {
        Objects.requireNonNull(filePath)
        if (! Files.exists(filePath)) {
            throw new IllegalArgumentException("${filePath} is not found")
        }
        Map<Metadata, ID> m = new HashMap<Metadata, ID>()
        //
        File file = filePath.toFile()
        String line
        file.withReader { reader ->
            while ((line = reader.readLine()) != null) {
                List<String> items = line.split('\\t') as List<String>
                if (items.size() > 0) {
                    // check if the left-most item is a valid SHA1 Digital
                    String idStr = items.remove(0)
                    if (! ID.isValid(idStr)) {
                        throw new IllegalStateException("id ${id} is not a valid ID")
                    }
                    ID id = new ID(idStr)
                    Metadata metadata = new Metadata(items)
                    m.put(metadata, id)
                } else {
                    ; // ignore blank lines
                }
            }
        }
        return m
    }


    Path getFile() {
        return file_
    }


    void put(Metadata metadata, ID id) {
        index_.put(metadata, id)
    }


    void serialize() {
        FileOutputStream fos = new FileOutputStream(file_.toFile())
        OutputStreamWriter osw = new OutputStreamWriter(fos, "utf-8")
        BufferedWriter br = new BufferedWriter(osw)
        for (Metadata metadata in index_.keySet()) {
            ID id = index_.get(metadata)
            StringBuilder sb = new StringBuilder()
            sb.append(id.toString())
            for (String entry in metadata) {
                sb.append("\t")
                sb.append(entry)
            }
            br.println(sb.toString())
        }
        br.flush()
        br.close()
    }

    @Override
    boolean equals(Object obj) {
        if (! obj instanceof Index) {
            return false
        }
        Index other = (Index)obj
        return file_ == file_ && index_ == index_
    }

    @Override
    int hashCode() {
        file_.hashCode()
    }

    @Override
    int compareTo(Object obj) {
        if (! obj instanceof Index) {
            throw new IllegalArgumentException("obj is not instance of Index")
        }
        Index other = (Index)obj
        return this.file_ <=> other.file_
    }
}
