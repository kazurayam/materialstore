package com.kazurayam.materialstore.core.filesystem;

import com.kazurayam.materialstore.core.util.JsonUtil;
import com.kazurayam.materialstore.core.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class Material implements Comparable<Material>, Jsonifiable, TemplateReady,
        Identifiable {

    private static final Logger logger = LoggerFactory.getLogger(Material.class.getName());

    public static final Material NULL_OBJECT =
            new Material(Store.NULL_OBJECT, JobName.NULL_OBJECT, JobTimestamp.NULL_OBJECT, IndexEntry.NULL_OBJECT);


    /*
     * empty but identifiable Material object is used as a filler
     * of a bachelor MaterialProduct
     */
    public static Material newEmptyMaterial() {
        return new Material(Store.NULL_OBJECT, JobName.NULL_OBJECT, JobTimestamp.NULL_OBJECT, IndexEntry.NULL_OBJECT,
                        StringUtils.generateRandomAlphaNumericString(7));
    }

    private final Store store_;
    private final JobName jobName_;
    private final JobTimestamp jobTimestamp_;
    private final IndexEntry indexEntry_;
    private final String randomId;

    public Material(Store store, JobName jobName, JobTimestamp jobTimestamp, IndexEntry indexEntry) {
        this(store, jobName, jobTimestamp, indexEntry, null);
    }

    public Material(Store store, JobName jobName, JobTimestamp jobTimestamp, IndexEntry indexEntry, String randomId) {
        Objects.requireNonNull(store);
        Objects.requireNonNull(jobName);
        Objects.requireNonNull(jobTimestamp);
        Objects.requireNonNull(indexEntry);
        this.store_ = store;
        this.jobName_ = jobName;
        this.jobTimestamp_ = jobTimestamp;
        this.indexEntry_ = indexEntry;
        // randomId may be null
        this.randomId = randomId;
    }

    public IFileType getFileType() {
        return this.getIndexEntry().getFileType();
    }

    public String getDescription() { return this.getIndexEntry().getMetadataDescription(); }

    public String getDescriptionSignature() {
        if (this.isEmpty()) {
            return this.randomId;
        } else {
            return this.getIndexEntry().getDescriptionSignature();
        }
    }

    public FileTypeDiffability getDiffability() {
        return this.getIndexEntry().getFileType().getDiffability();
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
     * @return the String of relative path of the MaterialIO file,
     * relative to the root dir.
     * On Mac and Linux, the path separator will be '/',
     * On Windows, the path separator will be '\'
     * <p>
     * Materialオブジェクトが表すファイルのパス（ただしrootを基底とする相対パス）のString表現を返す。
     * <p>
     * ああ、このメソッドひとつをきれいに実装するために materialstore を作ったのだ。
     * Materialsライブラリのぐちゃぐちゃさ加減に比べてこの実装の簡潔なことよ。
     */
    public Path getRelativePath() {
        Path root = store_.getRoot();
        logger.info("root.getClass().toString()=" + root.getClass().toString());
        Path p = root
                .resolve(jobName_.toString())
                .resolve(jobTimestamp_.toString())
                .resolve(Jobber.getOBJECTS_DIR_NAME())
                .resolve(this.getIndexEntry().getFileName());
        return root.relativize(p);
    }

    public Store getStore() {
        return this.store_;
    }

    /*
     * returns the byte array of the PNG file of "No Material is found"
     */
    public static byte[] loadNoMaterialFoundPng() throws MaterialstoreException {
        return readInputStream(getNoMaterialFoundPngAsInputStream());
    }

    /*
     * returns the byte array of the HTML file of "No Materials is found"
     */
    public static byte[] loadNoMaterialFoundText() throws MaterialstoreException {
        return readInputStream(getNoMaterialFoundHtmlAsInputStream());
    }

    public static InputStream getNoMaterialFoundPngAsInputStream() {
        ClassLoader cl = Material.class.getClassLoader();
        String resourcePath =
                "com/kazurayam/materialstore/core/filesystem" +
                        "/NoMaterialFound.png";
        InputStream inputStream = cl.getResourceAsStream(resourcePath);
        assert inputStream != null : "failed to load " + resourcePath + " from CLASSPATH";
        return inputStream;
    }

    public static InputStream getNoMaterialFoundHtmlAsInputStream() {
        ClassLoader cl = Material.class.getClassLoader();
        String resourcePath =
                "com/kazurayam/materialstore/core/filesystem" +
                        "/NoMaterialFound.html";
        InputStream inputStream = cl.getResourceAsStream(resourcePath);
        assert inputStream != null : "failed to load " + resourcePath + " from CLASSPATH";
        return inputStream;
    }

    public static byte[] readInputStream(InputStream inputStream) throws MaterialstoreException {
        // Buffer size taken to be 1024 say.
        byte[] buffer = new byte[1024];
        // we will use an object of ByteArrayOutputStream class
        // as a buffer to construct the entire byte[]
        ByteArrayOutputStream baos =
                new ByteArrayOutputStream();
        // load all bytes
        int len;
        try {
            while ((len = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
        } catch (IOException e) {
            throw new MaterialstoreException(e);
        }
        return baos.toByteArray();
    }


    /**
     * @return the returned value of getRelative() is stringified, and
     * replace all of `\` character to `/` to make it a valid relative URL string.
     * Material.NULL_OBJECT.getRelativeURL() will return a string which
     * represent the "No Material is Found" image encoded by base64.
     * It will like something like
     * "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAHElEQVQI12P4
     *   //8/w38GIAXDIBKE0DHxgljNBAAO9TXL0Y4OHwAAAABJRU5ErkJggg=="
     */
    public String getRelativeURL() {
        return this.getRelativePath().toString().replace("\\", "/");
    }

    @Override
    public ID getID() { return getIndexEntry().getID(); }

    @Override
    public String getShortID() {
        return getIndexEntry().getShortID();
    }

    /**
     * turn the Material object to a Path relative to the root.
     */
    public Path toPath(){
        return this.store_.getRoot().resolve(this.getRelativePath());
    }

    /*
     * returns a URL in the form of "file:/". The path will be an absolute path.
     *
     */
    public URL toURL() throws MaterialstoreException {
        try {
            return this.toPath().toUri().toURL();
        } catch (MalformedURLException e) {
            throw new MaterialstoreException(e);
        }
    }


    /*
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


    public boolean isEmpty() {
        return this.randomId != null;
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
        return this.toVariableJson(new SortKeys());
    }

    @Override
    public String toJson(boolean prettyPrint) {
        return this.toVariableJson(new SortKeys(), prettyPrint);
    }

    public String toVariableJson(SortKeys sortKeys) {
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
        //---------------------------------
        sb.append(this.getIndexEntry().getMetadata().toJson());

        //---------------------------------
        sb.append(",");
        sb.append("\"identification\":");
        sb.append("\"");
        sb.append(JsonUtil.escapeAsJsonString(this.getIndexEntry().getMetadata().getMetadataIdentification(sortKeys).toString()));
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


    public String toVariableJson(SortKeys sortKeys, boolean prettyPrint) {
        if (prettyPrint) {
            return JsonUtil.prettyPrint(toVariableJson(sortKeys));
        } else {
            return toJson();
        }
    }




}
