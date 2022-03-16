package com.kazurayam.materialstore.filesystem

import com.google.gson.Gson
import com.kazurayam.materialstore.util.GsonHelper
import com.kazurayam.materialstore.util.JsonUtil

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

final class Material implements Comparable, JSONifiable, TemplateReady {

    public static final Material NULL_OBJECT =
            new Material(JobName.NULL_OBJECT, JobTimestamp.NULL_OBJECT, IndexEntry.NULL_OBJECT)

    private final JobName jobName_
    private final JobTimestamp jobTimestamp_
    private final IndexEntry indexEntry_

    Material(JobName jobName, JobTimestamp jobTimestamp, IndexEntry indexEntry) {
        this.jobName_ = jobName
        this.jobTimestamp_ = jobTimestamp
        this.indexEntry_ = indexEntry
    }

    FileType getFileType() {
        return this.getIndexEntry().getFileType()
    }

    JobName getJobName() {
        return jobName_
    }

    JobTimestamp getJobTimestamp() {
        return jobTimestamp_
    }

    IndexEntry getIndexEntry() {
        return indexEntry_
    }

    Metadata getMetadata() {
        return this.getIndexEntry().getMetadata()
    }

    /**
     *
     * @return the relative path of the MObject file, relative to the root dir.
     * On Mac and Linux, the path separator will be '/',
     * On Windows, the path separator will be '\'
     *
     * Materialオブジェクトが表すファイルのパスただしrootを基底とする相対パスを返す。
     *
     * ああ、このメソッドひとつをきれいに実装するために materialstore を作ったのだ。
     * Materialsライブラリのぐちゃぐちゃさ加減に比べてこの実装の簡潔なことよ。
     */
    Path getRelativePath() {
        return Paths.get('.')
                .resolve(jobName_.toString())
                .resolve(jobTimestamp_.toString())
                .resolve(Jobber.OBJECTS_DIR_NAME)
                .resolve(this.getIndexEntry().getFileName())
                .normalize()
    }

    File toFile(Path root) {
        Objects.requireNonNull(root)
        if (! Files.exists(root)) {
            throw new IOException("${root.toString()} is not found")
        }
        Path p = root.resolve(getRelativePath())
        if (! Files.exists(p)) {
            throw new IOException("${p.toString()} is not found")
        }
        return p.toFile()
    }

    Path toPath(Path root) {
        return this.toFile(root).toPath()
    }

    /**
     * returns a URL in the form of "file:/". The path will be an absolute path.
     * @param root
     * @return URL of this Material
     */
    URL toURL(Path root) {
        return this.toFile(root).toURI().toURL()
    }


    /**
     * @return the returned value of getRelative() is stringified, and
     * replace all of `\` character to `/` to make it a valid relative URL string.
     */
    String getRelativeURL() {
        String s = this.getRelativePath().toString()
        return s.replace("\\", "/")
    }

    String getShortId() {
        return getIndexEntry().getShortId()
    }

    FileTypeDiffability getDiffability() {
        return this.getIndexEntry().getFileType().getDiffability()
    }

    /**
     * checks if this has the same FileType and Metadata as the other.
     * JobName and JobTimestamp are disregarded.
     */
    boolean isSimilar(Material other) {
        return this.getIndexEntry().equals(other.getIndexEntry())
    }

    @Override
    boolean equals(Object obj) {
        if (! obj instanceof Material) {
            return false
        }
        Material other = (Material)obj
        return this.getJobName() == other.getJobName() &&
                this.getJobTimestamp() == other.getJobTimestamp() &&
                this.getIndexEntry() == other.getIndexEntry()
    }

    @Override
    int hashCode() {
        int hash = 7
        hash = 31 * hash + this.getJobName().hashCode()
        hash = 31 * hash + this.getJobTimestamp().hashCode()
        hash = 31 * hash + this.getIndexEntry().hashCode()
        return hash
    }

    @Override
    String toString() {
        return toJson()
    }


    @Override
    int compareTo(Object obj) {
        if (! obj instanceof Material) {
            throw new IllegalArgumentException("obj is not an instance of Material")
        }
        Material other = (Material)obj
        int comparisonByJobName = this.getJobName() <=> other.getJobName()
        if (comparisonByJobName == 0) {
            int comparisonByJobTimestamp = this.getJobTimestamp() <=> other.getJobTimestamp()
            if (comparisonByJobTimestamp == 0) {
                return this.getIndexEntry() <=> other.getIndexEntry()
            } else {
                return comparisonByJobTimestamp
            }
        } else {
            return comparisonByJobName
        }
    }

    @Override
    String toJson() {
        StringBuilder sb = new StringBuilder()
        sb.append("{")
        sb.append("\"jobName\":\"" + this.getJobName() + "\"")
        sb.append(",")
        sb.append("\"jobTimestamp\":\"" + this.getJobTimestamp() + "\"")
        sb.append(",")
        sb.append("\"id\":\"" + this.getIndexEntry().getID().toString() + "\"")
        sb.append(",")
        sb.append("\"fileType\":\"" + this.getIndexEntry().getFileType().getExtension() + "\"")
        sb.append(",")
        sb.append("\"metadata\":")
        sb.append(this.getIndexEntry().getMetadata().toJson())
        sb.append(",")
        sb.append("\"metadataText\":")
        sb.append("\"" + JsonUtil.escapeAsJsonString(this.getIndexEntry().getMetadata().toJson()) + "\"")
        sb.append(",")
        sb.append("\"relativeUrl\":")
        sb.append("\"" + JsonUtil.escapeAsJsonString(getRelativeURL()) + "\"")
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
    Map<String, Object> toTemplateModel() {
        // convert JSON string to Java Map
        Map<String, Object> map = new Gson().fromJson(toJson(), Map.class)
        return map
    }

    @Override
    String toTemplateModelAsJson() {
        return toTemplateModelAsJson(false)
    }

    @Override
    String toTemplateModelAsJson(boolean prettyPrint) {
        Gson gson = GsonHelper.createGson(prettyPrint)
        Map<String, Object> model = toTemplateModel()
        return gson.toJson(model)
    }
}

