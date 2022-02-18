package com.kazurayam.materialstore.filesystem


import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest

final class MObject {

    private final byte[] data_

    private final FileType fileType_

    private static final int BUFFER_SIZE = 8000

    /*
    static String hash(byte[] data) {
        return DigestUtils.sha1Hex(data)
    }
     */

    static String hashJDK(byte[] data) {
        MessageDigest md = MessageDigest.getInstance("SHA1");
        md.update(data)
        byte[] digest = md.digest()
        StringBuffer sb = new StringBuffer()
        for (byte b : digest) {
            sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString()
    }

    MObject(byte[] data, FileType fileType) {
        Objects.requireNonNull(data)
        this.data_ = data
        this.fileType_ = fileType
    }

    ID getID() {
        return new ID(hashJDK(data_))
    }

    FileType getFileType() {
        return this.fileType_
    }

    String getFileName() {
        return "${getID().toString()}.${fileType_.getExtension()}"
    }

    byte[] getData() {
        return data_
    }

    Path resolvePath(Path objectsDir) {
        return objectsDir.resolve(this.getFileName())
    }

    boolean exists(Path objectsDir) {
        Path file = this.resolvePath(objectsDir)
        return Files.exists(file)
    }

    void serialize(Path objectsDir) {
        Objects.requireNonNull(objectsDir)
        //
        FileOutputStream fos = new FileOutputStream(objectsDir.toFile())
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

    static MObject deserialize(Path objectFile, FileType fileType) {
        Objects.requireNonNull(objectFile)
        Objects.requireNonNull(fileType)
        if (!Files.exists(objectFile)) {
            throw new IllegalArgumentException("${objectFile} is not present")
        }
        FileInputStream fis = new FileInputStream(objectFile.toFile())
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        byte[] buff = new byte[BUFFER_SIZE]
        int bytesRead
        while ((bytesRead = fis.read(buff)) != -1) {
            baos.write(buff, 0, bytesRead)
        }
        byte[] data = baos.toByteArray()
        fis.close()
        baos.close()
        return new MObject(data, fileType)
    }

    @Override
    boolean equals(Object obj) {
        if (! obj instanceof MObject) {
            return false
        }
        MObject other = (MObject)obj
        return this.getID() == other.getID() && this.getFileType() == other.getFileType()
    }

    @Override
    int hashCode() {
        int hash = 7
        hash = 31 * hash + this.getID().hashCode()
        return hash
    }
}
