package com.kazurayam.materialstore.differ

import com.kazurayam.materialstore.resolvent.DiffArtifactGroup

import java.nio.file.Path

interface DiffReporter {

    /**
     *
     * @param criteria the diffRatio of each DiffArtifact object is compared against
     * the criteria. if the diffRatio > the criteria, the DiffArtifact is regarded
     * "TO BE WARNED". The criteria is set to be 0.0 as default.
     */
    void setCriteria(Double criteria)

    /**
     *
     * @param diffArtifactGroup
     * @param reportFileName
     * @return number of diffActifacts exceeding the given criteria
     */
    Path reportDiffs(DiffArtifactGroup diffArtifacGgroup, String reportFileName)

}
