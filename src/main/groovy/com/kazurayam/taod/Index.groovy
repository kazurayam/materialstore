package com.kazurayam.taod

import groovy.json.JsonSlurper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Files
import java.nio.file.Path

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
 * 6141b40cfe9e7340a483a3097c4f6ff5d20e04ea\tpng\t["DevelopEnv","image/png","http://demoaut-mimic.kazurayam.com/"]
 *
 * How you construct and use a Metadata?
 * For example, a screenshot image file will be best described by a URL of the
 * Web page. You would certainly want to save the URL in the Metadata.
 * You would want to annotate the image file with further detail.
 * For Example, assuming you have a Web page may consists of 3 `<iframe id="frameX">`
 * HTML elements. Then you may want to take screenshots of each iframe elements.
 * In that case, you can append the id value of iframes to the Metadata.
 * It is up to you which information to be recoded as Metadata.
 * TAOD just stores what you gave. TAOD just retrieves the Metadata as you recorded.
 */
class Index implements Comparable {

    private static final Logger logger_ = LoggerFactory.getLogger(Index.class)

    private final Path indexFile_
    private final List<Tuple> lines_
    // Tuple of (ID, FileType, Metadata)

    Index() {
        lines_ = new ArrayList<Tuple>()
    }

    static Path getIndexFile(Path jobDir) {
        return jobDir.resolve("index")
    }

    void put(ID id, FileType fileType, Metadata metadata) {
        lines_.add(new Tuple(id, fileType, metadata))
    }

    void serialize(Path indexFile) {
        FileOutputStream fos = new FileOutputStream(indexFile.toFile())
        OutputStreamWriter osw = new OutputStreamWriter(fos, "utf-8")
        BufferedWriter br = new BufferedWriter(osw)
        lines_.each { Tuple tuple ->
            String s = formatLine(tuple)
            br.println(s)
        }
        br.flush()
        br.close()
    }

    static String formatLine(Tuple tuple) {
        Objects.requireNonNull(tuple)
        ID id = tuple[0]
        FileType ft = tuple[1]
        Metadata md = tuple[2]
        StringBuilder sb = new StringBuilder()
        sb.append(id.toString())
        sb.append("\t")
        sb.append(ft.getExtension())
        sb.append("\t")
        sb.append(md.toString())
        return sb.toString()
    }

    /**
     * read the "index" file, which is in the format like
     * ```
     * <Artifact ID>\t<Artifact Metadata[0]>\t<Metadata[1]>\t<Metadata[2]>...
     * ```
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
                    Tuple items = parseLine(line)
                    if (items != null) {
                        index.put(
                                (ID)items[0],
                                (FileType)items[1],
                                (Metadata)items[2])
                    }
                } catch (IndexParseException e) {
                    logger_.warn("LINE#=${x} \'${line}\' ${e.message()}")
                }
            }
        }
        return index
    }

    static Tuple parseLine(String line) throws IndexParseException {
        Objects.requireNonNull(line)
        List<String> items = line.split('\\t') as List<String>
        ID id = null
        FileType fileType = null
        Metadata metadata = null
        if (items.size() > 0) {
            String item1 = items[0]
            if (! ID.isValid(item1)) {
                throw new IndexParseException("invalid ID")
            }
            id = new ID(item1)
            if (items.size() > 1) {
                fileType = FileType.getByExtension(items[1])
                if (fileType == FileType.UNSUPPORTED) {
                    throw new IndexParseException("unsupported file extension")
                }
                if (items.size() > 2) {
                    try {
                        List<String> list = new JsonSlurper().parseText(items[2])
                        metadata = new Metadata(list)
                    } catch (Exception e) {
                        throw new IndexParseException("unable to parse metadata part")
                    }
                }
            }
        }
        if (id != null && fileType != null && metadata != null) {
            return new Tuple(id, fileType, metadata)
        }
        return null   // blank line returns null
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
    int compareTo(Object obj) {
        if (! obj instanceof Index) {
            throw new IllegalArgumentException("obj is not instance of Index")
        }
        Index other = (Index)obj
        return this.indexFile_ <=> other.indexFile_
    }
}
