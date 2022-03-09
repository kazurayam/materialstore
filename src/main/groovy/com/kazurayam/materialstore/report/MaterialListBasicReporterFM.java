package com.kazurayam.materialstore.report;

import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.filesystem.Store;

import java.nio.file.Path;
import java.util.Objects;

public class MaterialListBasicReporterFM extends MaterialListReporter {

    private Store store;
    private JobName jobName;

    public MaterialListBasicReporterFM(Store store, JobName jobName) {
        Objects.requireNonNull(store);
        Objects.requireNonNull(jobName);
        this.store = store;
        this.jobName = jobName;
    }

    @Override
    public Path report(MaterialList materialList, String reportFileName) {
        return null;
    }
}
