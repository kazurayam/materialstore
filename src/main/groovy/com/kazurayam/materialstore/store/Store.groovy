package com.kazurayam.materialstore.store

import com.kazurayam.materialstore.diff.DiffArtifact
import com.kazurayam.materialstore.diff.Differ
import com.kazurayam.materialstore.diff.Reporter

import java.awt.image.BufferedImage
import java.nio.charset.Charset
import java.nio.file.Path

/**
 * defines the public interface of the Store object
 */
interface Store {

    Path getRoot()

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
                   Charset charset)

    List<DiffArtifact> zipMaterialsToDiff(
            List<Material> expected,
            List<Material> actual,
            Set<String> metadataKeys)
}