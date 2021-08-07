package com.kazurayam.materialstore

import groovy.json.JsonOutput

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class Material implements Comparable {

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

    JobName getJobName() {
        return jobName_
    }

    JobTimestamp getJobTimestamp() {
        return jobTimestamp_
    }

    IndexEntry getIndexEntry() {
        return indexEntry_
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
     * @return the returned value of getRelative() is stringified, and
     * replace all of `\` character to `/` to make it a valid relative URL string.
     */
    String getRelativeURL() {
        String s = this.getRelativePath().toString()
        return s.replace("\\", "/")
    }

    boolean isImage() {
        FileType ft = this.getIndexEntry().getFileType()
        return ft == FileType.PNG || ft == FileType.JPEG ||
                ft == FileType.JPG || ft == FileType.GIF
    }

    boolean isText() {
        FileType ft = this.getIndexEntry().getFileType()
        return ft == FileType.CSV || ft == FileType.MD ||
                ft == FileType.HTML || ft == FileType.JSON ||
                ft == FileType.XML || ft == FileType.TXT
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
        Map m = ["jobName": this.getJobName().toString(),
                 "jobTimestamp": this.getJobTimestamp().toString(),
                 "ID": this.getIndexEntry().getID().toString(),
                 "fileType": this.getIndexEntry().getFileType().getExtension(),
                 "metadata": this.getIndexEntry().getMetadata().toString()
        ]
        return new JsonOutput().toJson(m)
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
}

