package com.kazurayam.materialstore.filesystem


import com.kazurayam.materialstore.util.JsonUtil
import groovy.json.JsonSlurper

import java.nio.file.Path
import java.nio.file.Paths

final class IndexEntry implements Comparable, Jsonifiable, TemplateReady {

    public static final IndexEntry NULL_OBJECT =
            new IndexEntry(
                    new MaterialIO(ID.NULL_OBJECT, FileType.NULL_OBJECT),
                    Metadata.NULL_OBJECT)

    private MaterialIO mio_
    private Metadata metadata_

    IndexEntry(MaterialIO mio, Metadata metadata) {
        this.mio_ = mio
        this.metadata_ = metadata
    }

    static IndexEntry parseLine(String line) throws IllegalArgumentException {
        Objects.requireNonNull(line)
        List<String> items = line.split('\\t') as List<String>
        ID id = null
        FileType fileType = null
        Metadata metadata = null
        if (items.size() > 0) {
            String item1 = items[0]
            if (! ID.isValid(item1)) {
                throw new IllegalArgumentException("invalid ID")
            }
            id = new ID(item1)
            if (items.size() > 1) {
                fileType = FileType.getByExtension(items[1])
                if (fileType == FileType.UNSUPPORTED) {
                    throw new IllegalArgumentException("unsupported file extension")
                }
                if (items.size() > 2) {
                    try {
                        Object obj = new JsonSlurper().parseText(items[2])
                        assert obj instanceof Map
                        metadata = Metadata.builder((Map)obj).build()
                    } catch (Exception e) {
                        throw new IllegalArgumentException("unable to parse metadata part")
                    }
                }
            }
        }
        if (id != null && fileType != null && metadata != null) {
            return new IndexEntry(new MaterialIO(id, fileType), metadata)
        }
        return null   // blank line returns null
    }

    private MaterialIO getMaterialIO() {
        return mio_
    }

    Path getFileName() {
        MaterialIO mio = getMaterialIO()
        return Paths.get(mio.getID().toString() + "." + mio.getFileType().getExtension())
    }

    FileType getFileType() {
        return getMaterialIO().getFileType()
    }

    ID getID() {
        return getMaterialIO().getID()
    }

    String getShortId() {
        return getID().getShortSha1()
    }

    Metadata getMetadata() {
        return metadata_
    }

    @Override
    boolean equals(Object obj) {
        if (! obj instanceof IndexEntry) {
            return false
        }
        IndexEntry other = (IndexEntry)obj
        return this.getFileType().equals(other.getFileType()) &&
                this.getMetadata().equals(other.getMetadata())
    }

    @Override
    int hashCode() {
        int hash = 7
        hash = 31 * hash + this.getMaterialIO().hashCode()
        hash = 31 * hash + this.getMetadata().hashCode()
        return hash
    }

    @Override
    String toString() {
        return toJson()
    }

    @Override
    String toJson() {
        StringBuilder sb = new StringBuilder()
        sb.append("{")
        sb.append("\"id\": " + this.getMaterialIO().getID().toJson())
        sb.append(",")
        sb.append("\"fileType\": " + this.getMaterialIO().getFileType().toJson())
        sb.append(",")
        sb.append("\"metadata\": " + this.getMetadata().toString())
        sb.append("}")
        return sb.toString()
    }

    @Override
    String toJson(boolean prettyPrint) {
        if (prettyPrint) {
            return JsonUtil.prettyPrint(toJson())
        } else {
            return toJson()
        }
    }


    @Override
    int compareTo(Object obj) {
        if (! obj instanceof IndexEntry) {
            throw new IllegalArgumentException("obj is not an instance of IndexEntry")
        }
        IndexEntry other = (IndexEntry)obj
        int comparisonByMetadata = this.getMetadata() <=> other.getMetadata()
        if (comparisonByMetadata == 0) {
            int comparisonByFileType = this.getFileType() <=> other.getFileType()
            if (comparisonByFileType == 0) {
                return this.getID() <=> other.getID()
            } else {
                return comparisonByFileType
            }
        } else {
            return comparisonByMetadata
        }
    }
}
