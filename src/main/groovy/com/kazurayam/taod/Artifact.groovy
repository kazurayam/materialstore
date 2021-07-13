package com.kazurayam.taod

import org.apache.commons.codec.digest.DigestUtils

import java.nio.file.Files
import java.nio.file.Path

class Artifact {

    private final byte[] data_

    private final FileType fileType_

    private static final int BUFFER_SIZE = 8000

    static String hash(byte[] data) {
        return DigestUtils.sha1Hex(data)
    }

    Artifact(byte[] data, FileType fileType) {
        Objects.requireNonNull(data)
        this.data_ = data
        this.fileType_ = fileType
    }

    ID getID() {
        return new ID(hash(data_))
    }

    FileType getFileType() {
        return this.fileType_
    }

    String getFileName() {
        return "${ID.toString()}.${fileType_.getExtension()}"
    }

    void serialize(Path artifactFile) {
        Objects.requireNonNull(artifactFile)
        //
        FileOutputStream fos = new FileOutputStream(artifactFile.toFile())
        ByteArrayInputStream bais = new ByteArrayInputStream(data_)
        byte[] buff = new byte[BUFFER_SIZE]
        int bytesRead
        while ((bytesRead = bais.read(buff)) != -1) {
            fos.write(buff, 0, bytesRead)
        }
        fos.flush()
        fos.close()
        bais.close()
    }

    static Artifact deserialize(Path artifactFile, FileType fileType) {
        Objects.requireNonNull(artifactFile)
        Objects.requireNonNull(fileType)
        if (!Files.exists(artifactFile)) {
            throw new IllegalArgumentException("${artifactFile} is not present")
        }
        FileInputStream fis = new FileInputStream(artifactFile.toFile())
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        byte[] buff = new byte[BUFFER_SIZE]
        int bytesRead
        while ((bytesRead = fis.read(buff)) != -1) {
            baos.write(buff, 0, bytesRead)
        }
        byte[] data = baos.toByteArray()
        fis.close()
        baos.close()
        return new Artifact(data, fileType)
    }

    @Override
    boolean equals(Object obj) {
        if (! obj instanceof Artifact) {
            return false
        }
        Artifact other = (Artifact)obj
        return this.getID() == other.getID() && this.getFileType() == other.getFileType()
    }

    @Override
    int hashCode() {
        int hash = 7
        hash = 31 * hash + this.getID().hashCode()
        return hash
    }
}
