package com.kazurayam.materialstore.differ


import com.kazurayam.materialstore.resolvent.ArtifactGroup

import java.nio.file.Path

interface DiffReporter {

    /**
     *
     * @param criteria the diffRatio of each Artifact object is compared against
     * the criteria. if the diffRatio > the criteria, the Artifact is regarded
     * "TO BE WARNED". The criteria is set to be 0.0 as default.
     */
    void setCriteria(Double criteria)

    /**
     *
     * @param artifactGroup
     * @param reportFileName
     * @return number of artifacts exceeding the given criteria
     */
    Path reportDiffs(ArtifactGroup artifactGroup, String reportFileName)

}
