package com.kazurayam.materialstore.report

import com.kazurayam.materialstore.MaterialstoreException
import com.kazurayam.materialstore.reduce.MProductGroup

import java.nio.file.Path

abstract class MProductGroupReporter extends AbstractReporter {

    /**
     *
     * @param criteria the diffRatio of each MProduct object is compared against
     * the criteria. if the diffRatio > the criteria, the MProduct is regarded
     * "TO BE WARNED". The criteria is set to be 0.0 as default.
     */
    abstract void setCriteria(Double criteria)

    abstract Path report(MProductGroup mProductGroup, String fileName) throws MaterialstoreException

    abstract void report(MProductGroup mProductGroup, Path filePath) throws MaterialstoreException

}
