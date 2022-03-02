package com.kazurayam.materialstore.filesystem

import com.kazurayam.materialstore.MaterialstoreException
import com.kazurayam.materialstore.map.IdentityMapper
import com.kazurayam.materialstore.map.MappedResultSerializer
import com.kazurayam.materialstore.map.Mapper
import com.kazurayam.materialstore.metadata.Metadata
import com.kazurayam.materialstore.metadata.QueryOnMetadata
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.temporal.TemporalUnit
import java.util.stream.Collectors

final class StoreImpl implements Store {

    private static final Logger logger = LoggerFactory.getLogger(StoreImpl.class)

    private final Path root_

    private final Set<Jobber> jobberCache_

    private static final int BUFFER_SIZE = 8000

    public static final String ROOT_DIRECTORY_NAME = "store"

    StoreImpl() {
        this(Paths.get(ROOT_DIRECTORY_NAME))
    }

    StoreImpl(Path root) {
        Objects.requireNonNull(root)
        // ensure the root directory to exist
        if (!Files.exists(root)) {
            Files.createDirectories(root)
        }
        this.root_ = root
        this.jobberCache_ = new HashSet<Jobber>()
    }

    @Override
    int copyMaterials(JobName jobName, JobTimestamp source, JobTimestamp target) {
        Objects.requireNonNull(jobName)
        Objects.requireNonNull(source)
        Objects.requireNonNull(target)
        MaterialList sourceMaterialList =
                this.select(jobName, source, QueryOnMetadata.ANY)
        MappedResultSerializer serializer =
                new MappedResultSerializer(this, jobName, target)
        Mapper identity = new IdentityMapper()
        identity.setStore(this)
        identity.setMappingListener(serializer)
        int count = 0
        for (Material material : sourceMaterialList) {
            identity.map(material)
            count++
        }
        return count
    }

    @Override
    int deleteMaterialsOlderThanExclusive(JobName jobName, JobTimestamp jobTimestamp,
                                          long amountToSubtract, TemporalUnit unit) {
        Objects.requireNonNull(jobName)
        Objects.requireNonNull(jobTimestamp)
        if (amountToSubtract < 0) {
            throw new IllegalArgumentException("amoutToSubtract(${amountToSubtract}) must not be a negative value < 0")
        }
        Objects.requireNonNull(unit)
        // calculate the base timestamp
        JobTimestamp thanThisJobTimestamp = jobTimestamp.minus(amountToSubtract, unit)
        // identify the JobTimestamp directories to be deleted
        List<JobTimestamp> toBeDeleted = this.findAllJobTimestampsPriorTo(jobName, thanThisJobTimestamp)
        // now delete files/directories
        int countDeleted = 0
        toBeDeleted.each { JobTimestamp jt ->
            Path dir = root_.resolve(jobName.toString()).resolve(jt.toString())
            // delete this directory recursively
            if (Files.exists(dir)) {
                // delete the directory to clear out using Java8 API
                Files.walk(dir)
                        .sorted(Comparator.reverseOrder())
                        .map {it.toFile() }
                        .forEach {
                            it.delete()
                            countDeleted += 1   // count the number of deleted files and directories
                        }
            }
        }
        return countDeleted
    }

    /**
     *
     * @param jobName
     * @return List of JobTimestamp objects in the jobName directory.
     * The returned list is sorted in reverse order (the latest timestamp should come first)
     * @throws com.kazurayam.materialstore.MaterialstoreException
     */
    @Override
    List<JobTimestamp> findAllJobTimestamps(JobName jobName)
            throws MaterialstoreException {
        Objects.requireNonNull(jobName)
        Path jobNameDir = root_.resolve(jobName.toString())
        if (Files.exists(jobNameDir)) {
            List<JobTimestamp> jobTimestamps =
                    Files.list(jobNameDir)
                            .filter { p -> Files.isDirectory(p) }
                            .map { p -> p.getFileName().toString() }
                            .filter { n -> JobTimestamp.isValid(n) }
                            .map { n -> new JobTimestamp(n) }
                            .collect(Collectors.toList())
            // sort the list in reverse order (the latest timestamp should come first)
            Collections.sort(jobTimestamps, Collections.reverseOrder())

            logger.debug(String.format("[findAllJobTimestamps] jobName=%s", jobName))
            logger.debug(String.format("[findAllJobTimestamps] jobTimestamps.size()=%d", jobTimestamps.size()))
            jobTimestamps.eachWithIndex { jt, index ->
                logger.debug(String.format("[findAllJobTimestamps] jt[%d]=%s", index, jt.toString()))
            }

            return jobTimestamps
        } else {
            throw new MaterialstoreException(
                    "JobName \"${jobName.toString()}\" is not found in ${root_}")
        }
    }

    @Override
    List<JobTimestamp> findAllJobTimestampsPriorTo(JobName jobName, JobTimestamp jobTimestamp)
            throws MaterialstoreException {
        Objects.requireNonNull(jobName)
        Objects.requireNonNull(jobTimestamp)
        List<JobTimestamp> all = findAllJobTimestamps(jobName)
        List<JobTimestamp> filtered =
                all.stream()
                        .filter { JobTimestamp jt ->
                            (jt <=> jobTimestamp) < 0
                        }
                        .collect(Collectors.toList())
        //Collections.reverse(filtered)
        return filtered
    }


    @Override
    JobTimestamp findJobTimestampPriorTo(JobName jobName, JobTimestamp jobTimestamp) {
        Objects.requireNonNull(jobName)
        Objects.requireNonNull(jobTimestamp)
        List<JobTimestamp> all = findAllJobTimestampsPriorTo(jobName, jobTimestamp)
        if (all.size() > 0) {
            return all.get(0)
        } else {
            return JobTimestamp.NULL_OBJECT
        }
    }

    @Override
    JobTimestamp findLatestJobTimestamp(JobName jobName) throws MaterialstoreException {
        Objects.requireNonNull(jobName)
        List<JobTimestamp> all = findAllJobTimestamps(jobName)
        if (all.size() > 0) {
            return all.get(0)
        } else {
            return JobTimestamp.NULL_OBJECT
        }
    }

    Jobber getCachedJobber(JobName jobName, JobTimestamp jobTimestamp) {
        Jobber result = null
        for (int i = 0; i < jobberCache_.size(); i++) {
            Jobber cached = jobberCache_[i]
            assert cached != null
            if (cached.getJobName() == jobName &&
                    cached.getJobTimestamp() == jobTimestamp) {
                result = cached
                break
            }
        }
        return result
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
    @Override
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

    @Override
    Path getPathOf(Material material) {
        Objects.requireNonNull(material)
        Path relativePath = material.getRelativePath()
        return this.getRoot().resolve(relativePath)
    }

    @Override
    Path getRoot() {
        return root_
    }

    @Override
    List<JobTimestamp> queryAllJobTimestamps(JobName jobName,
                                             QueryOnMetadata query) {
        Objects.requireNonNull(jobName)
        Objects.requireNonNull(query)
        List<JobTimestamp> all = findAllJobTimestamps(jobName)

        logger.debug(String.format("[queryAllJobTimestamps] all.size()=%d", all.size()))
        all.eachWithIndex { ajt, index ->
            logger.debug(String.format("[queryAllJobTimestamps] ajt[%d]=%s", index, ajt.toString()))
        }

        List<JobTimestamp> filtered = new ArrayList<>()
        all.each { jobTimestamp ->
            // select Material objects that match with the query given
            MaterialList materialList =
                    this.select(jobName, jobTimestamp, query)
            String msg = "[queryAllJobTimestamps] materialList.size()=${materialList.size()}, query=${query.toString()}"
            if (materialList.size() > 0) {
                logger.debug(msg)
                filtered.add(jobTimestamp)
            } else {
                logger.info(msg)
            }
        }

        logger.debug(String.format("[queryAllJobTimestamps] filtered.size()=%d", filtered.size()))
        filtered.eachWithIndex { fjt, index ->
            logger.debug(String.format("[queryAllJobTimestamps] fjt[%d]=%s", index, fjt.toString()))
        }

        return filtered
    }

    @Override
    List<JobTimestamp> queryAllJobTimestampsPriorTo(
            JobName jobName, QueryOnMetadata query, JobTimestamp jobTimestamp) {
        Objects.requireNonNull(jobTimestamp)
        List<JobTimestamp> all = this.queryAllJobTimestamps(jobName, query)

        logger.debug(String.format("[queryAllJobTimestampsPriorTo] all.size()=%d", all.size()))
        all.eachWithIndex { ajt, index ->
            logger.debug(String.format("[queryAllJobTimestampsPriorTo] aft[%d]=%s", index, ajt.toString()))
        }

        List<JobTimestamp> filtered =
                all.stream()
                        .filter { JobTimestamp jt ->
                            (jt <=> jobTimestamp) < 0
                        }
                        .collect(Collectors.toList())

        logger.debug(String.format("[queryAllJobTimestampsPriorTo] filtered.size()=%d", filtered.size()))
        filtered.eachWithIndex { fjt, index ->
            logger.debug(String.format("[queryAllJobTimestampsPriorTo] jft[%d]=%s", index, fjt.toString()))
        }

        return filtered
    }

    @Override
    JobTimestamp queryJobTimestampPriorTo(
            JobName jobName, QueryOnMetadata query, JobTimestamp jobTimestamp) {
        List<JobTimestamp> all =
                queryAllJobTimestampsPriorTo(jobName, query, jobTimestamp)
        logger.debug(String.format("[queryJobTimestampPriorTo] all.size()=%d", all.size()))
        if (all.size() > 0) {
            return all.get(0)
        } else {
            return JobTimestamp.NULL_OBJECT
        }
    }

    @Override
    JobTimestamp queryJobTimestampWithSimilarContentPriorTo(JobName jobName, JobTimestamp jobTimestamp) {
        return queryJobTimestampWithSimilarContentPriorTo(jobName, QueryOnMetadata.ANY, jobTimestamp)
    }

    @Override
    JobTimestamp queryJobTimestampWithSimilarContentPriorTo(JobName jobName, QueryOnMetadata query, JobTimestamp jobTimestamp) {
        Objects.requireNonNull(jobName)
        Objects.requireNonNull(jobTimestamp)
        List<JobTimestamp> all =
                queryAllJobTimestampsPriorTo(jobName, query, jobTimestamp)
        logger.debug(String.format("[queryJobTimestampWithSimilarContentPriorTo] all.size()=%d", all.size()))
        MaterialList baseList = this.select(jobName, jobTimestamp, QueryOnMetadata.ANY)
        logger.debug(String.format("[queryJobTimestampWithSimilarContentPriorTo] baseList.size()=%d", baseList.size()))
        assert baseList.size() > 0
        for (JobTimestamp previous : all) {
            logger.debug(String.format("[queryJobTimestampWithSimilarContentPriorTo] previous=%s", previous.toString()))
            MaterialList targetList = this.select(jobName, previous, QueryOnMetadata.ANY)
            if (similar(baseList, targetList)) {
                return previous
            }
        }
        return JobTimestamp.NULL_OBJECT
    }

    private static boolean similar(MaterialList baseList, MaterialList targetList) {
        int count = 0
        for (Material base : baseList) {
            if (targetList.containsSimilarMetadataAs(base)) {
                count += 1
            }
        }
        return count == baseList.size()
    }

    @Override
    JobTimestamp queryLatestJobTimestamp(JobName jobName, QueryOnMetadata query) {
        List<JobTimestamp> all = queryAllJobTimestamps(jobName, query)

        //all.each {
        //    logger.info("[queryLatestJobTimestamp] ${it.toString()}")
        //}

        if (all.size() > 0) {
            return all.get(0)
        } else {
            return JobTimestamp.NULL_OBJECT
        }
    }

    @Override
    byte[] read(Material material) {
        Objects.requireNonNull(material)
        Jobber jobber = this.getJobber(material.getJobName(), material.getJobTimestamp())
        assert jobber != null
        return jobber.read(material)
    }

    @Override
    MaterialList select(JobName jobName, JobTimestamp jobTimestamp,
                        QueryOnMetadata query, FileType fileType) {
        Jobber jobber = this.getJobber(jobName, jobTimestamp)
        return jobber.selectMaterials(query, fileType)
    }

    @Override
    MaterialList select(JobName jobName, JobTimestamp jobTimestamp,
                        QueryOnMetadata query) {
        Jobber jobber = this.getJobber(jobName, jobTimestamp)
        return jobber.selectMaterials(query)
    }

    @Override
    File selectFile(JobName jobName, JobTimestamp jobTimestamp,
                    QueryOnMetadata query, FileType fileType) {
        Jobber jobber = this.getJobber(jobName, jobTimestamp)
        MaterialList materials = jobber.selectMaterials(query, fileType)
        if (materials.size() > 0) {
            Material material = materials.get(0)
            File f = material.toFile(root_)
            return f
        } else {
            return null
        }
    }

    static byte[] toByteArray(InputStream inputStream) {
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

    @Override
    Material write(JobName jobName, JobTimestamp jobTimestamp,
                   FileType fileType, Metadata meta, BufferedImage input) {
        Objects.requireNonNull(input)
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        ImageIO.write(input, fileType.extension, baos)
        byte[] data = baos.toByteArray()
        baos.close()
        return this.write(jobName, jobTimestamp, fileType, meta, data)
    }

    @Override
    Material write(JobName jobName, JobTimestamp jobTimestamp,
                   FileType fileType, Metadata meta, byte[] input) {
        Objects.requireNonNull(root_)
        Objects.requireNonNull(jobName)
        Objects.requireNonNull(jobTimestamp)
        Objects.requireNonNull(meta)
        Objects.requireNonNull(fileType)
        Jobber jobber = this.getJobber(jobName, jobTimestamp)
        return jobber.write(input, fileType, meta)
    }

    @Override
    Material write(JobName jobName, JobTimestamp jobTimestamp,
                   FileType fileType, Metadata meta, File input) {
        Objects.requireNonNull(input)
        assert input.exists()
        FileInputStream fis = new FileInputStream(input)
        byte[] data = toByteArray(fis)
        fis.close()
        return this.write(jobName, jobTimestamp, fileType, meta, data)
    }

    @Override
    Material write(JobName jobName, JobTimestamp jobTimestamp,
                   FileType fileType, Metadata meta, Path input) {
        Objects.requireNonNull(input)
        assert Files.exists(input)
        FileInputStream fis = new FileInputStream(input.toFile())
        byte[] data = toByteArray(fis)
        fis.close()
        return this.write(jobName, jobTimestamp, fileType, meta, data)
    }

    @Override
    Material write(JobName jobName, JobTimestamp jobTimestamp,
                   FileType fileType, Metadata meta, String input,
                   Charset charset = StandardCharsets.UTF_8) {
        Objects.requireNonNull(input)
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        Writer wrt = new BufferedWriter(
                new OutputStreamWriter(baos, charset.name()))
        wrt.write(input)
        wrt.flush()
        byte[] data = baos.toByteArray()
        wrt.close()
        return this.write(jobName, jobTimestamp, fileType, meta, data)
    }

    @Override
    String toString() {
        return root_.toString()
    }
}
