package com.kazurayam.materialstore.store

import com.kazurayam.materialstore.diff.DiffArtifact

import java.awt.image.BufferedImage
import java.nio.charset.Charset
import java.nio.file.Path

/**
 * defines the public interface of the Store object
 */
interface Store {

    Path getRoot()

    List<Material> select(JobName jobName, JobTimestamp jobTimestamp,
                          MetadataPattern metadataPattern)

    List<Material> select(JobName jobName, JobTimestamp jobTimestamp,
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
                   FileType fileType, Metadata meta, String input,
                   Charset charset)

    List<DiffArtifact> zipMaterials(
            List<Material> expected,
            List<Material> actual,
            Set<String> metadataKeys)
}