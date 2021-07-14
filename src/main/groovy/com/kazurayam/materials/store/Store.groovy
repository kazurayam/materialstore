package com.kazurayam.materials.store

import com.kazurayam.materials.diff.DiffArtifact
import com.kazurayam.materials.diff.Differ
import com.kazurayam.materials.diff.Reporter

import java.awt.image.BufferedImage
import java.nio.file.Path

/**
 * defines the public interface of the Store object
 */
interface Store {

    Differ newDiffer(JobName jobName, JobTimestamp jobTimestamp)

    Reporter newReporter(JobName jobName, JobTimestamp jobTimestamp)

    List<Material> select(JobName jobName, JobTimestamp jobTimestamp,
                          FileType fileType, MetadataPattern metadataPattern)

    Material write(JobName jobName, JobTimestamp jobTimestamp,
                   Metadata meta, BufferedImage input, FileType fileType)

    Material write(JobName jobName, JobTimestamp jobTimestamp,
                   Metadata meta, byte[] input, FileType fileType)

    Material write(JobName jobName, JobTimestamp jobTimestamp,
                   Metadata meta, File input, FileType fileType)

    Material write(JobName jobName, JobTimestamp jobTimestamp,
                   Metadata meta, Path input, FileType fileType)

    Material write(JobName jobName, JobTimestamp jobTimestamp,
                   Metadata meta, String input, FileType fileType,
                   String charsetName)

    List<DiffArtifact> zipMaterialsToDiff(
            JobName jobName, JobTimestamp jobTimestamp,
            MetadataPattern pattern1, MetadataPattern pattern2)
}