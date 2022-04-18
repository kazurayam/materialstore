package com.kazurayam.materialstore.filesystem;

import com.kazurayam.materialstore.MaterialstoreException;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.time.temporal.TemporalUnit;
import java.util.List;

/**
 * defines the public interface of the Store object
 */
public interface Store {

    /**
     *
     * @param jobName
     * @param jobTimestamp
     * @param amountToSubtract
     * @param unit
     * @return number of JobTimestamp directories which were deleted
     * @throws MaterialstoreException
     */
    int deleteMaterialsOlderThanExclusive(JobName jobName, JobTimestamp jobTimestamp, long amountToSubtract, TemporalUnit unit) throws MaterialstoreException;

    int copyMaterials(JobName jobName, JobTimestamp source, JobTimestamp target) throws MaterialstoreException;

    List<JobTimestamp> findAllJobTimestamps(JobName jobName) throws IOException, MaterialstoreException;

    List<JobTimestamp> findAllJobTimestampsPriorTo(JobName jobName, JobTimestamp jobTimestamp) throws MaterialstoreException;

    JobTimestamp findJobTimestampPriorTo(JobName jobName, JobTimestamp jobTimestamp) throws MaterialstoreException;

    JobTimestamp findLatestJobTimestamp(JobName jobName) throws MaterialstoreException;

    List<JobTimestamp> queryAllJobTimestamps(JobName jobName, QueryOnMetadata query) throws MaterialstoreException;

    List<JobTimestamp> queryAllJobTimestampsPriorTo(JobName jobName, QueryOnMetadata query, JobTimestamp jobTimestamp) throws MaterialstoreException;

    JobTimestamp queryJobTimestampPriorTo(JobName jobName, QueryOnMetadata query, JobTimestamp jobTimestamp) throws MaterialstoreException;

    JobTimestamp queryLatestJobTimestamp(JobName jobName, QueryOnMetadata query) throws MaterialstoreException;

    byte[] read(Material material) throws MaterialstoreException;

    List<String> readAllLines(Material material) throws MaterialstoreException;

    List<String> readAllLines(Material material, Charset charset) throws MaterialstoreException;

    MaterialList reflect(MaterialList baseMaterialList) throws MaterialstoreException;

    MaterialList reflect(MaterialList baseMaterialList, JobTimestamp priorTo) throws MaterialstoreException;

    long retrieve(Material material, Path out) throws MaterialstoreException;

    Jobber getCachedJobber(JobName jobName, JobTimestamp jobTimestamp);

    Jobber getJobber(JobName jobName, JobTimestamp jobTimestamp) throws MaterialstoreException;

    Path getPathOf(Material material);

    Path getRoot();

    MaterialList select(JobName jobName, JobTimestamp jobTimestamp) throws MaterialstoreException;

    MaterialList select(JobName jobName, JobTimestamp jobTimestamp, QueryOnMetadata query) throws MaterialstoreException;

    MaterialList select(JobName jobName, JobTimestamp jobTimestamp, FileType fileType) throws MaterialstoreException;

    MaterialList select(JobName jobName, JobTimestamp jobTimestamp, FileType fileType, QueryOnMetadata query) throws MaterialstoreException;

    Material selectSingle(JobName jobName, JobTimestamp jobTimestamp, FileType fileType, QueryOnMetadata query) throws MaterialstoreException;

    Material write(JobName jobName, JobTimestamp jobTimestamp, FileType fileType, Metadata meta,
                   BufferedImage input) throws MaterialstoreException;

    Material write(JobName jobName, JobTimestamp jobTimestamp, FileType fileType, Metadata meta,
                   BufferedImage input, Jobber.DuplicationHandling flowControl) throws MaterialstoreException;

    Material write(JobName jobName, JobTimestamp jobTimestamp, FileType fileType, Metadata meta,
                   byte[] input) throws MaterialstoreException;

    Material write(JobName jobName, JobTimestamp jobTimestamp, FileType fileType, Metadata meta,
                   byte[] input, Jobber.DuplicationHandling flowControl) throws MaterialstoreException;

    Material write(JobName jobName, JobTimestamp jobTimestamp, FileType fileType, Metadata meta,
                   File input) throws MaterialstoreException;

    Material write(JobName jobName, JobTimestamp jobTimestamp, FileType fileType, Metadata meta,
                   File input, Jobber.DuplicationHandling flowControl) throws MaterialstoreException;

    Material write(JobName jobName, JobTimestamp jobTimestamp, FileType fileType, Metadata meta,
                   Path input) throws MaterialstoreException;

    Material write(JobName jobName, JobTimestamp jobTimestamp, FileType fileType, Metadata meta,
                   Path input, Jobber.DuplicationHandling flowControl) throws MaterialstoreException;

    Material write(JobName jobName, JobTimestamp jobTimestamp, FileType fileType, Metadata meta,
                   String input) throws MaterialstoreException;

    Material write(JobName jobName, JobTimestamp jobTimestamp, FileType fileType, Metadata meta,
                   String input, Jobber.DuplicationHandling flowControl) throws MaterialstoreException;

    Material write(JobName jobName, JobTimestamp jobTimestamp, FileType fileType, Metadata meta,
                   String input, Charset charset) throws MaterialstoreException;

    Material write(JobName jobName, JobTimestamp jobTimestamp, FileType fileType, Metadata meta,
                   String input, Charset charset, Jobber.DuplicationHandling flowControl)
            throws MaterialstoreException;

}
