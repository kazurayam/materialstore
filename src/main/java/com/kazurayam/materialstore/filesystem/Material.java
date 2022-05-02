package com.kazurayam.materialstore.filesystem;

import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public final class Material implements Comparable<Material>, Jsonifiable, TemplateReady,
        Identifiable {

    private static final Logger logger = LoggerFactory.getLogger(Material.class.getName());

    public static final Material NULL_OBJECT = new Material(JobName.NULL_OBJECT, JobTimestamp.NULL_OBJECT, IndexEntry.NULL_OBJECT);

    private final JobName jobName_;
    private final JobTimestamp jobTimestamp_;
    private final IndexEntry indexEntry_;

    public Material(JobName jobName, JobTimestamp jobTimestamp, IndexEntry indexEntry) {
        this.jobName_ = jobName;
        this.jobTimestamp_ = jobTimestamp;
        this.indexEntry_ = indexEntry;
    }

    public IFileType getFileType() {
        return this.getIndexEntry().getFileType();
    }

    public String getDescription() { return this.getIndexEntry().getDescription(); }

    public String getDescriptionSignature() { return this.getIndexEntry().getDescriptionSignature(); }

    public JobName getJobName() {
        return jobName_;
    }

    public JobTimestamp getJobTimestamp() {
        return jobTimestamp_;
    }

    public IndexEntry getIndexEntry() {
        return indexEntry_;
    }

    public Metadata getMetadata() {
        return this.getIndexEntry().getMetadata();
    }

    /**
     * @return the relative path of the MaterialIO file, relative to the root dir.
     * On Mac and Linux, the path separator will be '/',
     * On Windows, the path separator will be '\'
     * <p>
     * Materialオブジェクトが表すファイルのパスただしrootを基底とする相対パスを返す。
     * <p>
     * ああ、このメソッドひとつをきれいに実装するために materialstore を作ったのだ。
     * Materialsライブラリのぐちゃぐちゃさ加減に比べてこの実装の簡潔なことよ。
     */
    public Path getRelativePath() {
        return Paths.get(".").resolve(jobName_.toString()).resolve(jobTimestamp_.toString()).resolve(Jobber.getOBJECTS_DIR_NAME()).resolve(this.getIndexEntry().getFileName()).normalize();
    }

    public File toFile(final Store store) throws MaterialstoreException {
        return this.toFile(store.getRoot());
    }

    public File toFile(final Path root) throws MaterialstoreException {
        Objects.requireNonNull(root);
        if (!Files.exists(root)) {
            throw new MaterialstoreException(root + " is not found");
        }
        final Path p = root.resolve(getRelativePath());
        if (!Files.exists(p)) {
            throw new MaterialstoreException(p + " is not found");
        }
        return p.toFile();
    }

    public Path toPath(Path root) throws MaterialstoreException {
        return this.toFile(root).toPath();
    }

    public Path toPath(Store store) throws MaterialstoreException {
        return this.toFile(store.getRoot()).toPath();
    }

    /**
     * returns a URL in the form of "file:/". The path will be an absolute path.
     *
     */
    public URL toURL(Path root) throws MaterialstoreException {
        try {
            return this.toFile(root).toURI().toURL();
        } catch (MalformedURLException e) {
            throw new MaterialstoreException(e);
        }
    }

    /**
     * @return the returned value of getRelative() is stringified, and
     * replace all of `\` character to `/` to make it a valid relative URL string.
     */
    public String getRelativeURL() {
        String s = this.getRelativePath().toString();
        return s.replace("\\", "/");
    }

    @Override
    public String getId() { return getIndexEntry().getID().toString(); }

    @Override
    public String getShortId() {
        return getIndexEntry().getShortId();
    }

    public FileTypeDiffability getDiffability() {
        return this.getIndexEntry().getFileType().getDiffability();
    }

    /**
     * checks if this has the same FileType and Metadata as the other.
     * JobName and JobTimestamp are disregarded.
     */
    public boolean isSimilarTo(Material other) {
        boolean result = this.getIndexEntry().isSimilarTo(other.getIndexEntry());
        logger.trace(String.format("[isSimilarTo] %b, this=%s, other=%s",
                result, this.getDescription(), other.getDescription()));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if ( !(obj instanceof Material) ) {
            return false;
        }
        Material other = (Material) obj;
        return this.getJobName().equals(other.getJobName()) &&
                this.getJobTimestamp().equals(other.getJobTimestamp()) &&
                this.getIndexEntry().equals(other.getIndexEntry());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + this.getJobName().hashCode();
        hash = 31 * hash + this.getJobTimestamp().hashCode();
        hash = 31 * hash + this.getIndexEntry().hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return toJson();
    }

    @Override
    public int compareTo(Material other) {
        int comparisonByJobName = this.getJobName().compareTo(other.getJobName());
        if (comparisonByJobName == 0) {
            int comparisonByJobTimestamp = this.getJobTimestamp().compareTo(other.getJobTimestamp());
            if (comparisonByJobTimestamp == 0) {
                return this.getIndexEntry().compareTo(other.getIndexEntry());
            } else {
                return comparisonByJobTimestamp;
            }

        } else {
            return comparisonByJobName;
        }

    }

    @Override
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"jobName\":\"");
        sb.append(this.getJobName());
        sb.append("\"");
        sb.append(",");
        sb.append("\"jobTimestamp\":\"");
        sb.append(this.getJobTimestamp());
        sb.append("\"");
        sb.append(",");
        sb.append("\"id\":\"");
        sb.append(this.getIndexEntry().getID().toString());
        sb.append("\"");
        sb.append(",");
        sb.append("\"fileType\":\"");
        sb.append(this.getIndexEntry().getFileType().getExtension());
        sb.append("\"");
        sb.append(",");
        sb.append("\"metadata\":");
        sb.append(this.getIndexEntry().getMetadata().toJson());
        sb.append(",");
        sb.append("\"metadataText\":");
        sb.append("\"");
        sb.append(JsonUtil.escapeAsJsonString(this.getIndexEntry().getMetadata().toSimplifiedJson()));
        sb.append("\"");
        try {
            if (this.getIndexEntry().getMetadata().toURL() != null) {
                sb.append(",");
                sb.append("\"metadataURL\":");
                sb.append("\"");
                sb.append(JsonUtil.escapeAsJsonString(this.getIndexEntry().getMetadata().toURLAsString()));
                sb.append("\"");
            }
        } catch (MaterialstoreException e) {
            throw new IllegalStateException(e);
        }
        sb.append(",");
        sb.append("\"relativeUrl\":");
        sb.append("\"");
        sb.append(JsonUtil.escapeAsJsonString(getRelativeURL()));
        sb.append("\"");
        sb.append(",");
        sb.append("\"diffability\":");
        sb.append("\"");
        sb.append(JsonUtil.escapeAsJsonString(getDiffability().toString()));
        sb.append("\"");
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


}
