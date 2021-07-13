package com.kazurayam.taod

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path

class Organizer {

    private static final Logger logger = LoggerFactory.getLogger(Organizer.class)

    private final Path root_

    private final Set<Job> jobCache_

    private static int BUFFER_SIZE = 8000

    Organizer(Path root) {
        Objects.requireNonNull(root)
        Files.createDirectories(root)
        this.root_ = root
        this.jobCache_ = new HashSet<Job>()
    }

    Path getRoot() {
        return root_
    }


    ID write(JobName jobName, JobTimestamp jobTimestamp,
                 Metadata meta, File input, FileType fileType) {
        Objects.requireNonNull(input)
        assert input.exists()
        FileInputStream fis = new FileInputStream(input)
        byte[] data = toByteArray(fis)
        return this.write(jobName, jobTimestamp, meta, data, fileType)
    }

    ID write(JobName jobName, JobTimestamp jobTimestamp,
                 Metadata meta, Path input, FileType fileType) {
        Objects.requireNonNull(input)
        assert Files.exists(input)
        FileInputStream fis = new FileInputStream(input.toFile())
        byte[] data = toByteArray(fis)
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
        return baos.toByteArray()
    }


    ID write(JobName jobName, JobTimestamp jobTimestamp,
             Metadata meta, BufferedImage input, FileType fileType) {
        Objects.requireNonNull(input)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(input, fileType.extension, baos);
        byte[] data = baos.toByteArray()
        return this.write(jobName, jobTimestamp, meta, data, fileType)
    }


    ID write(JobName jobName, JobTimestamp jobTimestamp,
                 Metadata meta, byte[] input, FileType fileType) {
        Objects.requireNonNull(root_)
        Objects.requireNonNull(jobName)
        Objects.requireNonNull(jobTimestamp)
        Objects.requireNonNull(meta)
        Objects.requireNonNull(fileType)
        Job job = this.getJob(jobName, jobTimestamp)
        return job.commit(meta, input, fileType)
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
    Job getJob(JobName jobName, JobTimestamp jobTimestamp) {
        Job job = getCachedJob(jobName, jobTimestamp)
        if (job != null) {
            return job
        } else {
            Job newJob = new Job(root_, jobName, jobTimestamp)
            // put the new Job object in the cache
            jobCache_.add(newJob)
            return newJob
        }
    }


    Job getCachedJob(JobName jobName, JobTimestamp jobTimestamp) {
        Job result = null
        for (int i = 0; jobCache_.size(); i++) {
            Job cached = jobCache_[i]
            if (cached.getJobName() == jobName &&
                        cached.getJobTimestamp() == jobTimestamp) {
                result = cached
                break
            }
        }
        return result
    }

}
