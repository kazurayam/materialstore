package com.kazurayam.materialstore.filesystem


import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest

final class MObject {

    private final ID id_

    private final FileType fileType_

    private static final int BUFFER_SIZE = 8000

    /**
     * calculate SHA1 message digest of the given data
     */
    static String hashJDK(byte[] data) {
        MessageDigest md = MessageDigest.getInstance("SHA1")
        md.update(data)
        byte[] digest = md.digest()
        StringBuffer sb = new StringBuffer()
        for (byte b : digest) {
            sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1))
        }
        return sb.toString()
    }

    MObject(ID id, FileType fileType) {
        Objects.requireNonNull(id)
        Objects.requireNonNull(fileType)
        this.id_ = id
        this.fileType_ = fileType
    }

    ID getID() {
        return id_
    }

    FileType getFileType() {
        return this.fileType_
    }

    String getFileName() {
        return "${getID().toString()}.${getFileType().getExtension()}"
    }

    boolean existsInDir(Path objectsDir) {
        Path file = objectsDir.resolve(this.getFileName())
        return Files.exists(file)
    }

    static void serialize(byte[] data, Path objectsDir) {
        Objects.requireNonNull(objectsDir)
        //
        FileOutputStream fos = new FileOutputStream(objectsDir.toFile())
        ByteArrayInputStream bais = new ByteArrayInputStream(data)
        byte[] buff = new byte[BUFFER_SIZE]
        int bytesRead
        while ((bytesRead = bais.read(buff)) != -1) {
            fos.write(buff, 0, bytesRead)
        }
        fos.flush()
        fos.close()
        bais.close()
    }

    static byte[] deserialize(Path objectFile) {
        Objects.requireNonNull(objectFile)
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
        return data
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
