package com.kazurayam.materialstore.store

import java.awt.image.BufferedImage
import java.nio.charset.Charset
import java.nio.file.Path
import java.time.temporal.TemporalUnit

/**
 * defines the public interface of the Store object
 */
interface Store {

    Path getRoot()

    List<Material> select(JobName jobName, JobTimestamp jobTimestamp,
                          MetadataPattern metadataPattern)

    List<Material> select(JobName jobName, JobTimestamp jobTimestamp,
                          MetadataPattern metadataPattern, FileType fileType)

    File selectFile(JobName jobName, JobTimestamp jobTimestamp,
                    MetadataPattern metadataPattern, FileType fileType)

    Material write(JobName jobName, JobTimestamp jobTimestamp,
                   FileType fileType, Metadata meta, BufferedImage input)

    Material write(JobName jobName, JobTimestamp jobTimestamp,
                   FileType fileType, Metadata meta, byte[] input)

    Material write(JobName jobName, JobTimestamp jobTimestamp,
                   FileType fileType, Metadata meta, File input)

    Material write(JobName jobName, JobTimestamp jobTimestamp,
                   FileType fileType, Metadata meta, Path input)

    Material write(JobName jobName, JobTimestamp jobTimestamp,
                   FileType fileType, Metadata meta, String input)

    Material write(JobName jobName, JobTimestamp jobTimestamp,
                   FileType fileType, Metadata meta, String input,
                   Charset charset)

    DiffArtifacts zipMaterials(List<Material> expected, List<Material> actual, MetadataIgnoredKeys ignoredKeys)

    DiffArtifacts makeDiff(List<Material> expected, List<Material> actual)

    DiffArtifacts makeDiff(List<Material> expected, List<Material> actual, MetadataIgnoredKeys ignoredKeys)

    Path reportDiffs(JobName jobName, DiffArtifacts diffArtifacts, String fileName)

    List<JobTimestamp> findAllJobTimestamps(JobName jobName)

    List<JobTimestamp> findAllJobTimestampsPriorTo(JobName jobName, JobTimestamp jobTimestamp)

    JobTimestamp findLatestJobTimestamp(JobName jobName)

    JobTimestamp findJobTimestampPriorTo(JobName jobName, JobTimestamp jobTimestamp)

    int deleteMaterialsOlderThanExclusive(JobName jobName, JobTimestamp jobTimestamp,
                                          long amountToSubtract, TemporalUnit unit)
}