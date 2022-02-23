package com.kazurayam.materialstore

import com.kazurayam.materialstore.differ.DiffReporter
import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.MaterialList
import com.kazurayam.materialstore.resolvent.ArtifactGroup
import com.kazurayam.materialstore.resolvent.Resolvent
import com.kazurayam.materialstore.filesystem.Store
import com.kazurayam.materialstore.util.JsonUtil

import java.nio.file.Path

abstract class MaterialstoreFacade {

    /**
     * the factory method
     */
    static final MaterialstoreFacade newInstance(Store store) {
        return new MaterialstoreFacadeImpl(store)
    }

    abstract void addResolvent(Resolvent resolvent)

    abstract Path getRoot()

    abstract Store getStore()

    abstract Result makeDiffAndReport(JobName jobName, ArtifactGroup artifactGroup,
                                    Double criteria, String filename)

    abstract DiffReporter newReporter(JobName jobName)

    abstract Path reportArtifactGroup(JobName jobName, ArtifactGroup artifactGroup,
                              Double criteria, String fileName)

    abstract Path reportMaterials(JobName jobName, MaterialList materialList,
                         String fileName = "list.html")

    abstract ArtifactGroup workOn(ArtifactGroup input)

    static class Result {
        private Path report
        private Integer warnings
        Result(Path report) {
            this(report, 0)
        }
        Result(Path report, Integer warnings) {
            Objects.requireNonNull(report)
            Objects.requireNonNull(warnings)
            this.report = report
            this.warnings = warnings
        }
        Path report() {
            return report
        }
        Integer warnings() {
            return warnings
        }
        @Override
        String toString() {
            StringBuilder sb = new StringBuilder()
            sb.append("{")
            sb.append("\"report\":\"")
            sb.append(JsonUtil.escapeAsJsonString(this.report().toString()))
            sb.append("\",")
            sb.append("\"warnings\":")
            sb.append(this.warnings())
            sb.append("}")
            return sb.toString()
        }
    }
}
