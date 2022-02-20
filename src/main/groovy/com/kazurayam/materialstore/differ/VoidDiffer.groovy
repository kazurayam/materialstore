package com.kazurayam.materialstore.differ


import com.kazurayam.materialstore.resolvent.Artifact
import com.kazurayam.materialstore.filesystem.FileType
import com.kazurayam.materialstore.filesystem.Jobber
import com.kazurayam.materialstore.filesystem.Material
import com.kazurayam.materialstore.metadata.Metadata

import java.nio.charset.StandardCharsets
import java.nio.file.Path

/**
 * VoidDiffer does nothing
 */
class VoidDiffer implements Differ {

    Path root

    VoidDiffer() {}

    @Override
    void setRoot(Path root) {
        this.root = root
    }

    @Override
    Artifact makeArtifact(Artifact artifact) {
        Objects.requireNonNull(artifact)
        Objects.requireNonNull(artifact.getLeft())
        Objects.requireNonNull(artifact.getRight())
        Material left = artifact.getLeft()
        Material right = artifact.getRight()
        //
        StringBuilder sb = new StringBuilder()
        sb.append("Unable to take diff of binary files.\n\n")
        sb.append("left:  ")
        sb.append(left.toString())
        sb.append("\n\n")
        sb.append("right: ")
        sb.append(right.toString())
        sb.append("\n\n")
        String message =  sb.toString()
        byte[] diffData = message.getBytes(StandardCharsets.UTF_8)
        //
        Metadata diffMetadata = Metadata.builderWithMap([
                "category": "diff",
                "left": left.getIndexEntry().getID().toString(),
                "right": right.getIndexEntry().getID().toString()])
                .build()
        Jobber jobber = new Jobber(root, right.getJobName(), artifact.getResolventTimestamp())
        Material diffMaterial =
                jobber.write(diffData,
                        FileType.TXT,
                        diffMetadata,
                        Jobber.DuplicationHandling.CONTINUE)
        //
        Artifact result = new Artifact(artifact)
        result.setDiff(diffMaterial)
        return result
    }


}
