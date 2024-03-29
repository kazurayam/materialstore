package com.kazurayam.materialstore.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public final class MaterialIO {

    private final ID id_;
    private final IFileType fileType_;
    private static final int BUFFER_SIZE = 8000;

    /*
     * calculate SHA1 message digest of the given data
     */
    public static String hashJDK(byte[] data) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA1");
            md.update(data);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    public MaterialIO(ID id, IFileType fileType) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(fileType);
        this.id_ = id;
        this.fileType_ = fileType;
    }

    public ID getID() {
        return id_;
    }

    public IFileType getFileType() {
        return this.fileType_;
    }

    public String getFileName() {
        return getID() + "." + getFileType().getExtension();
    }

    public boolean existsInDir(Path objectsDir) {
        Path file = objectsDir.resolve(this.getFileName());
        return Files.exists(file);
    }

    public static void serialize(byte[] bytes, Path path) throws MaterialstoreException {
        Objects.requireNonNull(path);
        try {
            Files.write(path, bytes);
        } catch (IOException e) {
            throw new MaterialstoreException(e);
        }
    }

    public static byte[] deserialize(final Path objectFile) throws MaterialstoreException {
        Objects.requireNonNull(objectFile);
        if (!Files.exists(objectFile)) {
            throw new IllegalArgumentException(objectFile + " is not present");
        }
        try {
            /*
            FileInputStream fis = new FileInputStream(objectFile.toFile());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buff = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = fis.read(buff)) != -1) {
                baos.write(buff, 0, bytesRead);
            }
            byte[] data = baos.toByteArray();
            fis.close();
            baos.close();
            return data;
             */
            return Files.readAllBytes(objectFile);
        } catch (IOException e) {
            throw new MaterialstoreException(e);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if ( !(obj instanceof MaterialIO) ) {
            return false;
        }

        MaterialIO other = (MaterialIO) obj;
        return this.getID().equals(other.getID()) &&
                this.getFileType().equals(other.getFileType());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + this.getID().hashCode();
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":\"");
        sb.append(getID());
        sb.append("\"");
        sb.append(",");
        sb.append("}");
        sb.append("\"fileType\":\"");
        sb.append(getFileType());
        sb.append("\"");
        sb.append("}");
        return sb.toString();
    }

}
