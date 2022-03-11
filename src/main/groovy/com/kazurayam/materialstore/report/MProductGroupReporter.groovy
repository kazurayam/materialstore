package com.kazurayam.materialstore.report

import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.Store
import com.kazurayam.materialstore.reduce.MProductGroup

import java.nio.file.Path

abstract class MProductGroupReporter {

    /**
     *
     * @param criteria the diffRatio of each MProduct object is compared against
     * the criteria. if the diffRatio > the criteria, the MProduct is regarded
     * "TO BE WARNED". The criteria is set to be 0.0 as default.
     */
    abstract void setCriteria(Double criteria)

    abstract Path report(MProductGroup mProductGroup, String fileName)

    abstract void report(MProductGroup mProductGroup, Path filePath)

    static final MProductGroupReporter newInstance(Store store, JobName jobName) {
        return new MProductGroupBasicReporter(store, jobName)
    }

    static final String getTitle(Path file) {
        String fileName = file.getFileName().toString()
        return fileName.substring(0, fileName.indexOf(".html"))
    }

}
