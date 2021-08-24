package com.kazurayam.materialstore


import com.kazurayam.materialstore.reporter.DiffArtifactsBasicReporter
import com.kazurayam.materialstore.reporter.MaterialsBasicReporter
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
        Files.createDirectories(root)
        this.root_ = root
        this.jobberCache_ = new HashSet<Jobber>()
    }

    @Override
    Path getRoot() {
        return root_
    }

    @Override
    DiffArtifacts makeDiff(MaterialList left,
                           MaterialList right,
                           IgnoringMetadataKeys ignoringMetadataKeys = IgnoringMetadataKeys.NULL_OBJECT) {
        Objects.requireNonNull(left)
        Objects.requireNonNull(right)
        Objects.requireNonNull(ignoringMetadataKeys)

        DiffArtifacts diffArtifacts =
                this.zipMaterials(left, right, ignoringMetadataKeys)
        assert diffArtifacts != null

        DifferDriver differDriver = new DifferDriverImpl.Builder(root_).build()
        DiffArtifacts stuffedDiffArtifacts =
                differDriver.differentiate(diffArtifacts)

        return stuffedDiffArtifacts
    }

    DiffReporter newReporter(JobName jobName) {
        return new DiffArtifactsBasicReporter(root_, jobName)
    }

    @Override
    Path reportDiffs(JobName jobName, DiffArtifacts diffArtifacts, Double criteria, String fileName) {
        DiffReporter reporter = this.newReporter(jobName)
        reporter.setCriteria(criteria)
        reporter.reportDiffs(diffArtifacts, fileName)
        return root.resolve(fileName)
    }

    @Override
    Path reportMaterials(JobName jobName, MaterialList materialList,
                         String fileName = "list.html") {
        Objects.requireNonNull(jobName)
        Objects.requireNonNull(materialList)
        Objects.requireNonNull(fileName)
        MaterialsBasicReporter reporter =
                new MaterialsBasicReporter(this.root, jobName)
        return reporter.reportMaterials(materialList, fileName)
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
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(input, fileType.extension, baos);
        byte[] data = baos.toByteArray()
        baos.close()
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
    MaterialList select(JobName jobName, JobTimestamp jobTimestamp,
                          MetadataPattern metadataPattern, FileType fileType) {
        Jobber jobber = this.getJobber(jobName, jobTimestamp)
        return jobber.selectMaterials(metadataPattern, fileType)
    }

    @Override
    MaterialList select(JobName jobName, JobTimestamp jobTimestamp,
                          MetadataPattern metadataPattern) {
        Jobber jobber = this.getJobber(jobName, jobTimestamp)
        return jobber.selectMaterials(metadataPattern)
    }

    @Override
    File selectFile(JobName jobName, JobTimestamp jobTimestamp,
                    MetadataPattern metadataPattern, FileType fileType) {
        Jobber jobber = this.getJobber(jobName, jobTimestamp)
        MaterialList materials = jobber.selectMaterials(metadataPattern, fileType)
        if (materials.size() > 0) {
            Material material = materials.get(0)
            File f = material.toFile(root_)
            return f
        } else {
            return null
        }
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
     *
     * @param jobName
     * @return List of JobTimestamp objects in the jobName directory.
     * The returned list is sorted in reverse order (the latest timestamp should come first)
     * @throws MaterialstoreException
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
                            .map {p -> p.getFileName().toString() }
                            .filter { n -> JobTimestamp.isValid(n) }
                            .map {n -> new JobTimestamp(n) }
                            .collect(Collectors.toList())
            // sort the list in reverse order (the latest timestamp should come first)
            Collections.reverse(jobTimestamps)
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
                            jt.compareTo(jobTimestamp) < 0
                        }
                        .collect(Collectors.toList())
        Collections.reverse(filtered)
        return filtered
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

    /**
     *
     * @param jobName
     * @return null if directory of the jobName does not exists
     */
    List<Jobber> findJobbersOf(JobName jobName) {
        Path jobNamePath = root_.resolve(jobName.toString())
        if (! Files.exists(jobNamePath)) {
            return null
        }
        List<Jobber> result = Files.list(jobNamePath)
                .filter { Path p -> JobTimestamp.isValid(p.getFileName().toString() ) }
                .map { Path p -> new JobTimestamp(p.getFileName().toString()) }
                .map { JobTimestamp jt -> new Jobber(root_, jobName, jt) }
                .collect(Collectors.toList())
        return result
    }

    /**
     *
     * @param leftList
     * @param rightList
     * @param metadataKeys
     * @return
     */
    @Override
    DiffArtifacts zipMaterials(MaterialList leftList,
                               MaterialList rightList,
                               IgnoringMetadataKeys ignoringMetadataKeys) {
        Objects.requireNonNull(leftList)
        Objects.requireNonNull(rightList)
        Objects.requireNonNull(ignoringMetadataKeys)
        DiffArtifacts diffArtifacts = new DiffArtifacts()
        diffArtifacts.setLeftMaterialList(leftList)
        diffArtifacts.setRightMaterialList(rightList)
        diffArtifacts.setIgnoringMetadataKeys(ignoringMetadataKeys)
        //
        rightList.each { Material right->
            FileType rightFileType = right.getIndexEntry().getFileType()
            Metadata rightMetadata = right.getIndexEntry().getMetadata()
            MetadataPattern rightPattern =
                    MetadataPattern.builderWithMetadata(rightMetadata, ignoringMetadataKeys).build()
            //
            StringBuilder sb = new StringBuilder()
            sb.append("\nright pattern: ${rightPattern}\n")
            int foundLeftCount = 0
            leftList.each { Material left ->
                FileType leftFileType = left.getIndexEntry().getFileType()
                Metadata leftMetadata = left.getIndexEntry().getMetadata()
                if (leftFileType == rightFileType &&
                        leftMetadata.match(rightPattern)) {
                    DiffArtifact da =
                            new DiffArtifact.Builder(left, right)
                                    .descriptor(rightPattern)
                                    .build()
                    diffArtifacts.add(da)
                    sb.append("left metadata: Y ${leftMetadata}\n")
                    foundLeftCount += 1
                } else {
                    sb.append("left metadata: N ${leftMetadata}\n")
                }
            }
            if (foundLeftCount == 0) {
                DiffArtifact da =
                        new DiffArtifact.Builder(Material.NULL_OBJECT, right)
                                .descriptor(rightPattern)
                                .build()
                diffArtifacts.add(da)
            }
            if (foundLeftCount == 0 || foundLeftCount >= 2) {
                logger.warn(sb.toString())
            }
        }
        //
        leftList.each {Material left ->
            FileType leftFileType = left.getIndexEntry().getFileType()
            Metadata leftMetadata = left.getIndexEntry().getMetadata()
            MetadataPattern leftPattern =
                    MetadataPattern.builderWithMetadata(leftMetadata, ignoringMetadataKeys).build()
            //
            StringBuilder sb = new StringBuilder()
            sb.append("\nleft pattern: ${leftPattern}\n")
            int foundRightCount = 0
            rightList.each { Material right ->
                FileType rightFileType = right.getIndexEntry().getFileType()
                Metadata rightMetadata = right.getIndexEntry().getMetadata()
                if (rightFileType == leftFileType &&
                        rightMetadata.match(leftPattern)) {
                    ; // this must have been found matched already; no need to create a DiffArtifact
                    sb.append("right metadata: Y ${rightMetadata}\n")
                    foundRightCount += 1
                } else {
                    sb.append("right metadata: N ${rightMetadata}\n")
                }
            }
            if (foundRightCount == 0) {
                DiffArtifact da =
                        new DiffArtifact.Builder(left, Material.NULL_OBJECT)
                                .descriptor(leftPattern)
                                .build()
                diffArtifacts.add(da)
            }
            if (foundRightCount == 0 || foundRightCount >= 2) {
                logger.warn(sb.toString())
            }
        }
        //
        diffArtifacts.sort()
        return diffArtifacts
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
                        .forEach {it.delete();
                            countDeleted += 1   // count the number of deleted files and directories
                        }
            }
        }
        return countDeleted
    }

    @Override
    Path getPathOf(Material material) {
        Objects.requireNonNull(material)
        Path relativePath = material.getRelativePath()
        return this.getRoot().resolve(relativePath)
    }
}
