package com.kazurayam.materialstore.base.materialize;

import com.kazurayam.materialstore.core.filesystem.JobName;
import com.kazurayam.materialstore.core.filesystem.JobTimestamp;
import com.kazurayam.materialstore.core.filesystem.Jsonifiable;
import com.kazurayam.materialstore.core.filesystem.Store;
import com.kazurayam.materialstore.core.util.JsonUtil;

public final class StorageDirectory implements Jsonifiable {
    private final Store store;
    private final JobName jobName;
    private final JobTimestamp jobTimestamp;
    public StorageDirectory(Store store, JobName jobName, JobTimestamp jobTimestamp) {
        this.store = store;
        this.jobName = jobName;
        this.jobTimestamp = jobTimestamp;
    }
    public Store getStore() {
        return this.store;
    }
    public JobName getJobName() {
        return this.jobName;
    }
    public JobTimestamp getJobTimestamp() {
        return this.jobTimestamp;
    }
    @Override
    public String toString() {
        return this.toJson(true);
    }

    @Override
    public String toJson() {
        return this.toJson(false);
    }

    @Override
    public String toJson(boolean prettyPrint) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"store\":\"");
        sb.append(store.toString());
        sb.append("\",");
        sb.append("\"jobName\":\"");
        sb.append(jobName.toString());
        sb.append("\",");
        sb.append("\"jobTimestam\":\"");
        sb.append(jobTimestamp.toString());
        sb.append("\"");
        sb.append("}");
        if (prettyPrint) {
            return JsonUtil.prettyPrint(sb.toString());
        } else {
            return sb.toString();
        }
    }
}
