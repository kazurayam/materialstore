package com.kazurayam.taod

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors

class Store {

    private static final Logger logger = LoggerFactory.getLogger(Store.class)

    private final Path root_

    private final Set<Jobber> jobberCache_

    private static int BUFFER_SIZE = 8000

    Store(Path root) {
        Objects.requireNonNull(root)
        Files.createDirectories(root)
        this.root_ = root
        this.jobberCache_ = new HashSet<Jobber>()
    }

    Path getRoot() {
        return root_
    }


    Material write(JobName jobName, JobTimestamp jobTimestamp,
                 Metadata meta, File input, FileType fileType) {
        Objects.requireNonNull(input)
        assert input.exists()
        FileInputStream fis = new FileInputStream(input)
        byte[] data = toByteArray(fis)
        fis.close()
        return this.write(jobName, jobTimestamp, meta, data, fileType)
    }

    Material write(JobName jobName, JobTimestamp jobTimestamp,
                 Metadata meta, Path input, FileType fileType) {
        Objects.requireNonNull(input)
        assert Files.exists(input)
        FileInputStream fis = new FileInputStream(input.toFile())
        byte[] data = toByteArray(fis)
        fis.close()
        return this.write(jobName, jobTimestamp, meta, data, fileType)
    }


    private static byte[] toByteArray(InputStream inputStream) {
        Objects.requireNonNull(inputStream)
        byte[] buff = new byte[BUFFER_SIZE]
        int bytesRead
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        while ((bytesRead = inputStream.read(buff)) != -1) {
            baos.write(buff, 0, bytesRead)
        }
        inputStream.close()
        return baos.toByteArray()
    }


    Material write(JobName jobName, JobTimestamp jobTimestamp,
             Metadata meta, BufferedImage input, FileType fileType) {
        Objects.requireNonNull(input)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(input, fileType.extension, baos);
        byte[] data = baos.toByteArray()
        baos.close()
        return this.write(jobName, jobTimestamp, meta, data, fileType)
    }


    Material write(JobName jobName, JobTimestamp jobTimestamp,
                 Metadata meta, byte[] input, FileType fileType) {
        Objects.requireNonNull(root_)
        Objects.requireNonNull(jobName)
        Objects.requireNonNull(jobTimestamp)
        Objects.requireNonNull(meta)
        Objects.requireNonNull(fileType)
        Jobber jobber = this.getJobber(jobName, jobTimestamp)
        return jobber.commit(meta, input, fileType)
    }


    /**
     * return an instance of Job.
     * if cached, return the found.
     * if not cached, return the new one.
     *
     * @param jobName
     * @param jobTimestamp
     * @return
     */
    Jobber getJobber(JobName jobName, JobTimestamp jobTimestamp) {
        Jobber jobber = getCachedJobber(jobName, jobTimestamp)
        if (jobber != null) {
            return jobber
        } else {
            Jobber newJob = new Jobber(root_, jobName, jobTimestamp)
            // put the new Job object in the cache
            jobberCache_.add(newJob)
            return newJob
        }
    }


    Jobber getCachedJobber(JobName jobName, JobTimestamp jobTimestamp) {
        Jobber result = null
        for (int i = 0; jobberCache_.size(); i++) {
            Jobber cached = jobberCache_[i]
            if (cached.getJobName() == jobName &&
                        cached.getJobTimestamp() == jobTimestamp) {
                result = cached
                break
            }
        }
        return result
    }

    /**
     *
     * @param jobName
     * @return null if directory of the jobName does not exists
     */
    List<Jobber> listJobberOf(JobName jobName) {
        Path jobNamePath = root_.resolve(jobName.toString())
        if (! Files.exists(jobNamePath)) {
            return null
        }
        List<Jobber> result = Files.list(jobNamePath)
                .filter { Path p -> JobTimestamp.isValidFormat(p.getFileName().toString() ) }
                .map { Path p -> new JobTimestamp(p.getFileName().toString()) }
                .map { JobTimestamp jt -> new Jobber(root_, jobName, jt) }
                .collect(Collectors.toList())
        return result
    }
}
