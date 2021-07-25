package com.kazurayam.materialstore.store

import com.kazurayam.materialstore.store.reporter.DiffReporterToHTML
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors

class StoreImpl implements Store {

    private static final Logger logger = LoggerFactory.getLogger(StoreImpl.class)

    private final Path root_

    private final Set<Jobber> jobberCache_

    private static final int BUFFER_SIZE = 8000

    private static final String ROOT_DIRECTORY_NAME = "Materials"

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
    DiffArtifacts makeDiff(List<Material> expected,
                                List<Material> actual,
                                Set<String> metadataKeys) {
        Objects.requireNonNull(expected)
        Objects.requireNonNull(actual)
        Objects.requireNonNull(metadataKeys)

        DiffArtifacts diffArtifacts =
                this.zipMaterials(expected, actual, metadataKeys)
        assert diffArtifacts != null

        DifferDriver differDriver = new DifferDriverImpl.Builder(root_).build()
        DiffArtifacts stuffedDiffArtifacts =
                differDriver.differentiate(diffArtifacts)

        return stuffedDiffArtifacts
    }

    @Override
    DiffReporter newReporter(JobName jobName) {
        return new DiffReporterToHTML(root_, jobName)
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
    List<Material> select(JobName jobName, JobTimestamp jobTimestamp,
                          MetadataPattern metadataPattern, FileType fileType) {
        Jobber jobber = this.getJobber(jobName, jobTimestamp)
        return jobber.selectMaterials(metadataPattern, fileType)
    }

    @Override
    List<Material> select(JobName jobName, JobTimestamp jobTimestamp,
                          MetadataPattern metadataPattern) {
        Jobber jobber = this.getJobber(jobName, jobTimestamp)
        return jobber.selectMaterials(metadataPattern)
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
     * @param expectedList
     * @param actualList
     * @param metadataKeys
     * @return
     */
    @Override
    DiffArtifacts zipMaterials(List<Material> expectedList,
                                    List<Material> actualList,
                                    Set<String> metadataKeys) {
        Objects.requireNonNull(expectedList)
        Objects.requireNonNull(actualList)
        Objects.requireNonNull(metadataKeys)
        DiffArtifacts diffArtifacts = new DiffArtifacts()
        //
        actualList.each { Material actual->
            FileType actualFileType = actual.getIndexEntry().getFileType()
            Metadata actualMetadata = actual.getIndexEntry().getMetadata()
            MetadataPattern pattern = MetadataPattern.create(metadataKeys, actualMetadata)
            expectedList.each { Material expected ->
                FileType expectedFileType = expected.getIndexEntry().getFileType()
                Metadata expectedMetadata = expected.getIndexEntry().getMetadata()
                if (expectedFileType == actualFileType && expectedMetadata.match(pattern)) {
                    DiffArtifact da = new DiffArtifact(expected, actual, pattern)
                    diffArtifacts.add(da)
                } else {
                    ;
                }
            }
        }
        //
        diffArtifacts.sort()
        return diffArtifacts
    }
}
