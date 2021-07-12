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

    static Artifact deserialize(Path blobsDir, ID id) {
        Objects.requireNonNull(blobsDir)
        Objects.requireNonNull(id)
        Path blob = blobsDir.resolve(id.toString())
        if (!Files.exists(blob)) {
            throw new IllegalArgumentException("${blob} is not present")
        }
        FileInputStream fis = new FileInputStream(blob.toFile())
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        byte[] buff = new byte[BUFFER_SIZE]
        int bytesRead
        while ((bytesRead = fis.read(buff)) != -1) {
            baos.write(buff, 0, bytesRead)
        }
        byte[] data = baos.toByteArray()
        fis.close()
        baos.close()
    }

    Artifact(byte[] data, FileType fileType) {
        Objects.requireNonNull(data)
        this.data_ = data
        this.fileType_ = fileType
    }

    ID getID() {
        return new ID(hash(data_))
    }

    void serialize(Path blobs) {
        Objects.requireNonNull(blobs)
        Path blobPath = blobs.resolve(this.getID().toString())
        //
        FileOutputStream fos = new FileOutputStream(blobPath.toFile())
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

    @Override
    boolean equals(Object obj) {
        if (! obj instanceof Artifact) {
            return false
        }
        Artifact other = (Artifact)obj
        return
    }

    @Override
    int hashCode() {
        int hash = 7
        hash = 31 * hash + this.getID().hashCode()
        return hash
    }
}
