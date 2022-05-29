package com.kazurayam.materialstore.filesystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
final class Index implements Iterable<IndexEntry> {

    private static final Logger logger_ = LoggerFactory.getLogger(Index.class);

    private final List<IndexEntry> lines_;
    // Tuple of (ID, FileType, Metadata)

    public Index() {
        lines_ = new ArrayList<IndexEntry>();
    }

    public static Path getIndexFile(Path jobDir) {
        return jobDir.resolve("index");
    }

    public List<IndexEntry> indexEntriesOf(IFileType fileType, Metadata metadata) {
        return lines_.stream()
                        .filter(ie ->
                                ie.getFileType() == fileType && ie.getMetadata() == metadata)
                        .collect(Collectors.toList());
    }

    public boolean containsKey(IFileType fileType, Metadata metadata) {
        List<IndexEntry> filtered = this.indexEntriesOf(fileType, metadata);
        return (filtered.size() > 0);
    }

    public IndexEntry put(ID id, IFileType fileType, Metadata metadata)
            throws MaterialstoreException {
        if (this.containsKey(fileType, metadata)) {
            throw new MaterialstoreException("the combination of " +
                    "fileType:${fileType.getExtension()} and " +
                    "metadata:${metadata.toString()} is already " +
                    "there in the index");
        }
        IndexEntry indexEntry = new IndexEntry(new MaterialIO(id, fileType), metadata);
        lines_.add(indexEntry);
        return indexEntry;
    }

    public int size() {
        return lines_.size();
    }

    public Iterator<IndexEntry> iterator() {
        return lines_.iterator();
    }

    /**
     * write data int "index" file.
     * lines are sorted by the order of Metadata > FileType > ID
     */
    public void serialize(Path indexFile) throws MaterialstoreException {
        try {
            FileOutputStream fos = new FileOutputStream(indexFile.toFile());
            OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
            PrintWriter pw = new PrintWriter(osw);
            List<IndexEntry> sorted = lines_.stream().sorted().collect(Collectors.toList());
            sorted.forEach(indexEntry -> {
                        String s = formatLine(indexEntry);
                        pw.println(s);
            });
            pw.flush();
            pw.close();
        } catch (FileNotFoundException e) {
            throw new MaterialstoreException(e);
        }
    }

    public static String formatLine(IndexEntry indexEntry) {
        Objects.requireNonNull(indexEntry);
        ID id = indexEntry.getID();
        IFileType ft = indexEntry.getFileType();
        Metadata md = indexEntry.getMetadata();
        StringBuilder sb = new StringBuilder();
        sb.append(id.toString());
        sb.append("\t");
        sb.append(ft.getExtension());
        sb.append("\t");
        sb.append(md.toSimplifiedJson());
        return sb.toString();
    }

    /**
     * read the "index" file
     *
     */
    public static Index deserialize(Path indexFile) throws MaterialstoreException {
        Objects.requireNonNull(indexFile);
        if (! Files.exists(indexFile)) {
            throw new IllegalArgumentException("${indexFile} is not found");
        }
        Index index = new Index();
        //
        File file = indexFile.toFile();
        try {
            try (BufferedReader reader =
                         new BufferedReader(
                                 new InputStreamReader(
                                         new FileInputStream(file), StandardCharsets.UTF_8))) {
                String line;
                int x = 0;
                while ((line = reader.readLine()) != null) {
                    x += 1;
                    try {
                        IndexEntry indexEntry = IndexEntry.parseLine(line);
                        if (indexEntry != null) {
                            index.put(
                                    indexEntry.getID(),
                                    indexEntry.getFileType(),
                                    indexEntry.getMetadata());
                        }
                    } catch (IllegalArgumentException e) {
                        logger_.warn("LINE#=${x} '${line}' ${e.getMessage()}");
                    }
                }
            }
        } catch (IOException e) {
            throw new MaterialstoreException(e);
        }
        return index;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"indexEntries\": [");
        int count = 0;
        for (IndexEntry line : lines_) {
            if (count > 0) {
                sb.append(",");
            }
            sb.append(line.toString());
            count += 1;
        }
        sb.append("]");
        sb.append("}");
        return sb.toString();
    }
}

