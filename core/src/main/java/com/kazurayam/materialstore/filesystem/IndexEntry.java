package com.kazurayam.materialstore.filesystem;

import com.google.gson.Gson;
import com.kazurayam.materialstore.util.GsonHelper;
import com.kazurayam.materialstore.util.JsonUtil;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class IndexEntry implements Comparable<IndexEntry>, Jsonifiable, TemplateReady {

    public static final IndexEntry NULL_OBJECT =
            new IndexEntry(
                    new MaterialIO(ID.NULL_OBJECT, FileType.NULL_OBJECT),
                    Metadata.NULL_OBJECT);

    private final MaterialIO mio_;
    private final Metadata metadata_;

    public IndexEntry(MaterialIO mio, Metadata metadata) {
        this.mio_ = mio;
        this.metadata_ = metadata;
    }

    static IndexEntry parseLine(String line) throws IllegalArgumentException {
        Objects.requireNonNull(line);
        List<String> items = Arrays.asList(line.split("\\t"));
        ID id = null;
        FileType fileType = null;
        Metadata metadata = null;
        if (items.size() > 0) {
            String item1 = items.get(0);
            if (! ID.isValid(item1)) {
                throw new IllegalArgumentException("invalid ID");
            }
            id = new ID(item1);
            if (items.size() > 1) {
                fileType = FileTypeUtil.getByExtension(items.get(1));
                if (fileType == FileType.UNSUPPORTED) {
                    throw new IllegalArgumentException("unsupported file extension");
                }
                if (items.size() > 2) {
                    try {
                        Gson gson = new Gson();
                        Map<String, String> map = GsonHelper.toStringStringMap(
                                gson.fromJson(items.get(2), Map.class));
                        metadata = Metadata.builder(map).build();
                    } catch (Exception e) {
                        throw new IllegalArgumentException("unable to parse metadata part");
                    }
                }
            }
        }
        if (id != null && fileType != null && metadata != null) {
            return new IndexEntry(new MaterialIO(id, fileType), metadata);
        }
        return null;   // blank line returns null
    }

    public MaterialIO getMaterialIO() {
        return mio_;
    }

    public Path getFileName() {
        MaterialIO mio = getMaterialIO();
        return Paths.get(mio.getID().toString() + "." + mio.getFileType().getExtension());
    }

    public String getMetadataDescription() {
        return this.getFileType().getExtension() + " " + this.getMetadata().getMetadataDescription();
    }

    public String getDescriptionSignature() {
        String descriptionSignature =
                MaterialIO.hashJDK(this.getMetadataDescription().getBytes(StandardCharsets.UTF_8));
        return descriptionSignature.substring(0, 7);
    }

    public IFileType getFileType() {
        return getMaterialIO().getFileType();
    }

    public ID getID() {
        return getMaterialIO().getID();
    }

    public String getShortId() {
        return getID().getShortSha1();
    }

    public Metadata getMetadata() {
        return metadata_;
    }

    public boolean isSimilarTo(IndexEntry other) {
        return this.getFileType().equals(other.getFileType()) &&
                this.getMetadata().equals(other.getMetadata());
    }

    @Override
    public boolean equals(Object obj) {
        if ( !(obj instanceof IndexEntry)) {
            return false;
        }
        IndexEntry other = (IndexEntry)obj;
        return this.getID().equals(other.getID()) &&
                this.getFileType().equals(other.getFileType()) &&
                this.getMetadata().equals(other.getMetadata());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + this.getMaterialIO().hashCode();
        hash = 31 * hash + this.getMetadata().hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return toJson();
    }

    @Override
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\": ");
        sb.append(this.getMaterialIO().getID().toJson());
        sb.append(",");
        sb.append("\"fileType\": ");
        sb.append(this.getMaterialIO().getFileType().toJson());
        sb.append(",");
        sb.append("\"metadata\": ");
        sb.append(this.getMetadata().toString());
        sb.append("}");
        return sb.toString();
    }

    @Override
    public String toJson(boolean prettyPrint) {
        if (prettyPrint) {
            return JsonUtil.prettyPrint(toJson());
        } else {
            return toJson();
        }
    }


    @Override
    public int compareTo(IndexEntry other) {
        Objects.requireNonNull(other);
        int comparisonByMetadata = this.getMetadata().compareTo(other.getMetadata());
        if (comparisonByMetadata == 0) {
            int comparisonByFileType = compareIFileTypes(this.getFileType(), other.getFileType());
            if (comparisonByFileType == 0) {
                return this.getID().compareTo(other.getID());
            } else {
                return comparisonByFileType;
            }
        } else {
            return comparisonByMetadata;
        }
    }

    private int compareIFileTypes(IFileType left, IFileType right) {
        String leftExtension = left.getExtension();
        String rightExtension = right.getExtension();
        return leftExtension.compareTo(rightExtension);
    }
}
