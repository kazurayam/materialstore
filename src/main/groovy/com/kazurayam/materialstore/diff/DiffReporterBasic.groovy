package com.kazurayam.materialstore.diff


import com.kazurayam.materialstore.store.Material
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Files
import java.nio.file.Path

import groovy.json.JsonOutput

class DiffReporterBasic implements DiffReporter {

    private static final Logger logger = LoggerFactory.getLogger(DiffReporterBasic.class)

    private final Path root_

    DiffReporterBasic(Path root) {
        Objects.requireNonNull(root)
        ensureRoot(root)
        this.root_ = root
    }

    private static void ensureRoot(Path root) {
        Objects.requireNonNull(root)
        if (! Files.exists(root)) {
            throw new IllegalArgumentException("${root} is not present")
        }
    }

    @Override
    void reportDiffs(List<DiffArtifact> diffArtifacts, Path reportFile) {
        StringBuilder sb = new StringBuilder()
        sb.append("# DiffArtifacts\n\n")
        diffArtifacts.eachWithIndex { DiffArtifact da, int index ->
            sb.append("## #${index} \n")
            sb.append(makeListItem("expected", da.getExpected()))
            sb.append(makeListItem("actual", da.getActual()))
            sb.append(makeListItem("diff", da.getDiff()))
            sb.append("\n\n")
        }
        reportFile.toFile().text = sb.toString()
    }

    private static String makeListItem(String name, Material material) {
        StringBuilder sb = new StringBuilder()
        sb.append("### ${name}\n")
        if (material.isImage()) {
            sb.append("![${name}](${material.getRelativeURL()})\n")
        } else if (material.isText()) {
            sb.append("[${material.getRelativeURL()}](${material.getRelativeURL()})\n")
        } else {
            // files other than image and text, no way to render in Markdown
            ;
        }
        sb.append("- URL: `${material.getRelativeURL()}`\n")
        String s = JsonOutput.prettyPrint(material.getIndexEntry().getMetadata().toString())
        sb.append("- metadata: `${s}`\n")
        return sb.toString()
    }
}