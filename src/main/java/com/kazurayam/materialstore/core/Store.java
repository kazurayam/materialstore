package com.kazurayam.materialstore.core;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * defines the public interface of the Store object
 */
public interface Store {

    Store NULL_OBJECT = new StoreImpl(null);

    /*
     * return true if the directory of specified as JobName/JobTimestamp
     * exists in the store
     * @param jobName
     * @param jobTimestamp
     * @return
     */
    boolean contains(JobName jobName, JobTimestamp jobTimestamp) throws MaterialstoreException;

    boolean contains(JobName jobName) throws MaterialstoreException;

    int copyMaterials(JobName jobName, JobTimestamp source, JobTimestamp target) throws MaterialstoreException;

    int deleteJobName(JobName jobName) throws MaterialstoreException;

    int deleteJobTimestamp(JobName jobName, JobTimestamp jobTimestamp) throws MaterialstoreException;

    List<JobName> findAllJobNames() throws MaterialstoreException;

    List<JobTimestamp> findAllJobTimestamps(JobName jobName) throws MaterialstoreException;

    List<JobTimestamp> findAllJobTimestampsPriorTo(JobName jobName, JobTimestamp jobTimestamp) throws MaterialstoreException;

    List<Path> findAllReportsOf(JobName jobName) throws MaterialstoreException;

    List<JobTimestamp> findDifferentiatingJobTimestamps(JobName jobName) throws MaterialstoreException;

    /*
     * returns true if the specfied JobName/JobTimestamp directory contains one or more
     * Differentiating IndexEntry.
     * @param jobName
     * @param jobTimestamp
     * @return
     * @throws MaterialstoreException
     */
    boolean hasDifferentiatingIndexEntry(JobName jobName, JobTimestamp jobTimestamp) throws MaterialstoreException;

    Set<JobTimestamp> markOlderThan(JobName jobName, JobTimestamp olderThan)
            throws MaterialstoreException;

    Set<JobTimestamp> markNewerThanOrEqualTo(JobName jobName, JobTimestamp newerThanOrEqualTo)
            throws MaterialstoreException;

    List<JobTimestamp> findJobTimestampsReferredBy(JobName jobName, JobTimestamp jobTimestamp) throws MaterialstoreException;

    JobTimestamp findJobTimestampPriorTo(JobName jobName, JobTimestamp jobTimestamp) throws MaterialstoreException;


    JobTimestamp findLatestJobTimestamp(JobName jobName) throws MaterialstoreException;

    /*
     *
     * @param jobName
     * @param nth starts with 1; does not start with zero
     * @return
     * @throws MaterialstoreException
     */
    JobTimestamp findNthJobTimestamp(JobName jobName, int nth) throws MaterialstoreException;

    Jobber getCachedJobber(JobName jobName, JobTimestamp jobTimestamp);

    Jobber getJobber(JobName jobName, JobTimestamp jobTimestamp) throws MaterialstoreException;

    Path getPathOf(JobName jobName) throws MaterialstoreException;

    Path getPathOf(JobName jobName, JobTimestamp jobTimestamp) throws MaterialstoreException;

    Path getPathOf(Material material);

    Path getRoot();

    List<JobTimestamp> queryAllJobTimestamps(JobName jobName, QueryOnMetadata query) throws MaterialstoreException;

    List<JobTimestamp> queryAllJobTimestampsPriorTo(JobName jobName, QueryOnMetadata query, JobTimestamp jobTimestamp) throws MaterialstoreException;

    JobTimestamp queryJobTimestampPriorTo(JobName jobName, QueryOnMetadata query, JobTimestamp jobTimestamp) throws MaterialstoreException;

    JobTimestamp queryLatestJobTimestamp(JobName jobName, QueryOnMetadata query) throws MaterialstoreException;

    byte[] read(Material material) throws MaterialstoreException;

    List<String> readAllLines(Material material) throws MaterialstoreException;

    List<String> readAllLines(Material material, Charset charset) throws MaterialstoreException;

    MaterialList reflect(MaterialList baseMaterialList) throws MaterialstoreException;

    MaterialList reflect(MaterialList baseMaterialList, JobTimestamp priorTo) throws MaterialstoreException;

    String resolveReportFileName(JobName jobName, JobTimestamp jobTimestamp);

    long retrieve(Material material, Path out) throws MaterialstoreException;

    MaterialList select(JobName jobName, JobTimestamp jobTimestamp) throws MaterialstoreException;

    MaterialList select(JobName jobName, JobTimestamp jobTimestamp, QueryOnMetadata query) throws MaterialstoreException;

    MaterialList select(JobName jobName, JobTimestamp jobTimestamp, IFileType fileType) throws MaterialstoreException;

    MaterialList select(JobName jobName, JobTimestamp jobTimestamp, IFileType fileType, QueryOnMetadata query) throws MaterialstoreException;

    Material selectSingle(JobName jobName, JobTimestamp jobTimestamp) throws MaterialstoreException;

    Material selectSingle(JobName jobName, JobTimestamp jobTimestamp, IFileType fileType) throws MaterialstoreException;

    Material selectSingle(JobName jobName, JobTimestamp jobTimestamp, QueryOnMetadata query) throws MaterialstoreException;

    Material selectSingle(JobName jobName, JobTimestamp jobTimestamp, IFileType fileType, QueryOnMetadata query) throws MaterialstoreException;

    Material write(JobName jobName, JobTimestamp jobTimestamp, IFileType fileType, Metadata meta,
                   BufferedImage input) throws MaterialstoreException;

    Material write(JobName jobName, JobTimestamp jobTimestamp, IFileType fileType, Metadata meta,
                   BufferedImage input, Jobber.DuplicationHandling flowControl) throws MaterialstoreException;

    Material write(JobName jobName, JobTimestamp jobTimestamp, IFileType fileType, Metadata meta,
                   byte[] input) throws MaterialstoreException;

    Material write(JobName jobName, JobTimestamp jobTimestamp, IFileType fileType, Metadata meta,
                   byte[] input, Jobber.DuplicationHandling flowControl) throws MaterialstoreException;

    Material write(JobName jobName, JobTimestamp jobTimestamp, IFileType fileType, Metadata meta,
                   File input) throws MaterialstoreException;

    Material write(JobName jobName, JobTimestamp jobTimestamp, IFileType fileType, Metadata meta,
                   File input, Jobber.DuplicationHandling flowControl) throws MaterialstoreException;

    Material write(JobName jobName, JobTimestamp jobTimestamp, IFileType fileType, Metadata meta,
                   Path input) throws MaterialstoreException;

    Material write(JobName jobName, JobTimestamp jobTimestamp, IFileType fileType, Metadata meta,
                   Path input, Jobber.DuplicationHandling flowControl) throws MaterialstoreException;

    Material write(JobName jobName, JobTimestamp jobTimestamp, IFileType fileType, Metadata meta,
                   String input) throws MaterialstoreException;

    Material write(JobName jobName, JobTimestamp jobTimestamp, IFileType fileType, Metadata meta,
                   String input, Jobber.DuplicationHandling flowControl) throws MaterialstoreException;

    Material write(JobName jobName, JobTimestamp jobTimestamp, IFileType fileType, Metadata meta,
                   String input, Charset charset) throws MaterialstoreException;

    Material write(JobName jobName, JobTimestamp jobTimestamp, IFileType fileType, Metadata meta,
                   String input, Charset charset, Jobber.DuplicationHandling flowControl)
            throws MaterialstoreException;

}
