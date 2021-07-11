package com.kazurayam.taod

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.imageio.ImageIO
import javax.imageio.stream.ImageInputStream
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path

class Organizer {

    private static Logger logger = LoggerFactory.getLogger(Organizer.class)

    private Path root_
    private Set<Index> indexSet_
    private static int BUFFER_SIZE = 8000

    Organizer(Path root) {
        Objects.requireNonNull(root)
        Files.createDirectories(root)
        this.root_ = root
        this.indexSet_ = new HashSet<Index>()
    }

    Path getRoot() {
        return root_
    }


    ID write(JobName jobName, JobTimestamp jobTimestamp,
                 Metadata meta, File file) {
        FileInputStream fis = new FileInputStream(file)
        byte[] data = toByteArray(fis)
        return this.write(jobName, jobTimestamp, meta, data)
    }

    ID write(JobName jobName, JobTimestamp jobTimestamp,
                 Metadata meta, Path path) {
        FileInputStream fis = new FileInputStream(path.toFile())
        byte[] data = toByteArray(fis)
        return this.write(jobName, jobTimestamp, meta, data)
    }

    ID write(JobName jobName, JobTimestamp jobTimestamp,
                 Metadata meta, BufferedImage bufferedImage) {
        ImageInputStream iis = ImageIO.createImageInputStream(bufferedImage)
        byte[] data = toByteArray(iis)
        return this.write(jobName, jobTimestamp, meta, data)
    }

    private static byte[] toByteArray(InputStream inputStream) {
        byte[] buff = new byte[BUFFER_SIZE]
        int bytesRead
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        while ((bytesRead = inputStream.read(buff)) != -1) {
            baos.write(buff, 0, bytesRead)
        }
        return baos.toByteArray()
    }

    ID write(JobName jobName, JobTimestamp jobTimestamp,
                 Metadata meta, byte[] data) {
        Artifacts artifacts = new Artifacts(root_, jobName, jobTimestamp)
        return artifacts.commit(meta, data)
    }


    private Index getIndex(JobName jobName, JobTimestamp jobTimestamp) {
        Path file = root_.resolve(jobName.toString()).resolve(jobTimestamp.toString())
        for (Index cached in indexSet_) {
            if (cached.getFile() == file) {
                return cached
            }
        }
        Index newIndex = new Index(root_, jobName, jobTimestamp)
        indexSet_.add(newIndex)
        return newIndex
    }

}
