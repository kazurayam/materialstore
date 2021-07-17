package com.kazurayam.materialstore.report

import com.kazurayam.materialstore.diff.DiffArtifact
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Files
import java.nio.file.Path

class BasicDiffReporter implements DiffReporter {

    private static final Logger logger = LoggerFactory.getLogger(BasicDiffReporter.class)

    private final Path root_

    BasicDiffReporter(Path root) {
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
        sb.append("# DiffArtifact\n\n")
        diffArtifacts.eachWithIndex { DiffArtifact da, int index ->
            sb.append("## ${index} \n")
            sb.append("- expected : ${da.getExpected().getIndexEntry().getID().toString()}\n")
            sb.append("- actual   : ${da.getActual().getIndexEntry().getID().toString()}\n")
            sb.append("- diff     : ${da.getDiff().getIndexEntry().getID().toString()}\n")
            sb.append("\n\n")
        }
        reportFile.toFile().text = sb.toString()
    }

}
