package com.kazurayam.materialstore.filesystem;

import com.kazurayam.materialstore.util.JsonUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public final class Material implements Comparable<Material>, Jsonifiable, TemplateReady {
    public Material(JobName jobName, JobTimestamp jobTimestamp, IndexEntry indexEntry) {
        this.jobName_ = jobName;
        this.jobTimestamp_ = jobTimestamp;
        this.indexEntry_ = indexEntry;
    }

    public FileType getFileType() {
        return this.getIndexEntry().getFileType();
    }

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

    public File toFile(final Path root) throws IOException {
        Objects.requireNonNull(root);
        if (!Files.exists(root)) {
            throw new IOException(root + " is not found");
        }

        final Path p = root.resolve(getRelativePath());
        if (!Files.exists(p)) {
            throw new IOException(p + " is not found");
        }

        return p.toFile();
    }

    public Path toPath(Path root) throws IOException {
        return this.toFile(root).toPath();
    }

    /**
     * returns a URL in the form of "file:/". The path will be an absolute path.
     *
     */
    public URL toURL(Path root) throws IOException {
        return this.toFile(root).toURI().toURL();
    }

    /**
     * @return the returned value of getRelative() is stringified, and
     * replace all of `\` character to `/` to make it a valid relative URL string.
     */
    public String getRelativeURL() {
        String s = this.getRelativePath().toString();
        return s.replace("\\", "/");
    }

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
    public boolean isSimilar(Material other) {
        return this.getIndexEntry().equals(other.getIndexEntry());
    }

    @Override
    public boolean equals(Object obj) {
        if ( !(obj instanceof Material) ) {
            return false;
        }
        Material other = (Material) obj;
        return this.getJobName().equals(other.getJobName()) && this.getJobTimestamp().equals(other.getJobTimestamp()) && this.getIndexEntry().equals(other.getIndexEntry());
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
        if (this.getIndexEntry().getMetadata().toURL() != null) {
            sb.append(",");
            sb.append("\"metadataURL\":");
            sb.append("\"");
            sb.append(JsonUtil.escapeAsJsonString(this.getIndexEntry().getMetadata().toURLAsString()));
            sb.append("\"");
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

    public static final Material NULL_OBJECT = new Material(JobName.NULL_OBJECT, JobTimestamp.NULL_OBJECT, IndexEntry.NULL_OBJECT);
    private final JobName jobName_;
    private final JobTimestamp jobTimestamp_;
    private final IndexEntry indexEntry_;
}
