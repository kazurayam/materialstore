package com.kazurayam.materialstore.core.filesystem;

public final class JobName implements Comparable<JobName>, Jsonifiable {

    public JobName(final String jobName) {
        if (!isValid(jobName)) {
            throw new IllegalArgumentException("\"" + jobName + "\" is not a valid JobName");
        }
        this.jobName = jobName;
    }

    public static boolean isValid(String s) throws IllegalArgumentException {
        if (s.equals("_")) {
            return true;
        }
        return Filename.isValid(s);
    }

    @Override
    public String toJson() {
        return "\"" + jobName + "\"";
    }

    @Override
    public String toJson(boolean prettyPrint) {
        return toJson();
    }

    @Override
    public boolean equals(Object obj) {
        if ( !(obj instanceof JobName) ) {
            return false;
        }
        JobName other = (JobName) obj;
        return this.jobName.equals(other.getJobName());
    }

    @Override
    public int hashCode() {
        return jobName.hashCode();
    }

    @Override
    public String toString() {
        return jobName;
    }

    @Override
    public int compareTo(JobName other) {
        return this.jobName.compareTo(other.getJobName());
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public static final JobName NULL_OBJECT = new JobName("_");

    private String jobName;
}
