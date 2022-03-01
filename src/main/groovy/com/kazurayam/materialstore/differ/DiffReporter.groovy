package com.kazurayam.materialstore.differ


import com.kazurayam.materialstore.resolvent.MProductGroup

import java.nio.file.Path

interface DiffReporter {

    /**
     *
     * @param criteria the diffRatio of each MProduct object is compared against
     * the criteria. if the diffRatio > the criteria, the MProduct is regarded
     * "TO BE WARNED". The criteria is set to be 0.0 as default.
     */
    void setCriteria(Double criteria)

    /**
     *
     * @param mProductGroup
     * @param reportFileName
     * @return number of mProducts exceeding the given criteria
     */
    Path reportDiffs(MProductGroup mProductGroup, String reportFileName)

}
