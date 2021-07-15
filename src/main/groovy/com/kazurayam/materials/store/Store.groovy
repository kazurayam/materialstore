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
                   FileType fileType, Metadata meta, BufferedImage input)

    Material write(JobName jobName, JobTimestamp jobTimestamp,
                   FileType fileType, Metadata meta, byte[] input)

    Material write(JobName jobName, JobTimestamp jobTimestamp,
                   FileType fileType, Metadata meta, File input)

    Material write(JobName jobName, JobTimestamp jobTimestamp,
                   FileType fileType, Metadata meta, Path input)

    Material write(JobName jobName, JobTimestamp jobTimestamp,
                   FileType fileType, Metadata meta, String input,
                   String charsetName)

    List<DiffArtifact> zipMaterialsToDiff(
            JobName jobName, JobTimestamp jobTimestamp, FileType fileType,
            MetadataPattern pattern1, MetadataPattern pattern2)
}