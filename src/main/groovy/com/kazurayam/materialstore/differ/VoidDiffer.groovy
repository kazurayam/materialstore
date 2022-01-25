package com.kazurayam.materialstore.differ

import com.kazurayam.materialstore.DiffArtifact
import com.kazurayam.materialstore.Differ
import com.kazurayam.materialstore.FileType
import com.kazurayam.materialstore.Jobber
import com.kazurayam.materialstore.Material
import com.kazurayam.materialstore.Metadata

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
    DiffArtifact makeDiffArtifact(DiffArtifact input) {
        Objects.requireNonNull(input)
        Objects.requireNonNull(input.getLeft())
        Objects.requireNonNull(input.getRight())
        Material left = input.getLeft()
        Material right = input.getRight()
        //
        StringBuilder sb = new StringBuilder()
        sb.append("Unable to take diff.\n")
        sb.append("left:  ")
        sb.append(left.toString())
        sb.append("\n")
        sb.append("right: ")
        sb.append(right.toString())
        sb.append("\n")
        String message =  sb.toString()
        byte[] diffData = message.getBytes(StandardCharsets.UTF_8)
        //
        Metadata diffMetadata = Metadata.builderWithMap([
                "category": "diff",
                "left": left.getIndexEntry().getID().toString(),
                "right": right.getIndexEntry().getID().toString()])
                .build()
        Jobber jobber = new Jobber(root, right.getJobName(), right.getJobTimestamp())
        Material diffMaterial =
                jobber.write(diffData,
                        FileType.TXT,
                        diffMetadata,
                        Jobber.DuplicationHandling.CONTINUE)
        //
        DiffArtifact result = new DiffArtifact(input)
        result.setDiff(diffMaterial)
        return result
    }


}
