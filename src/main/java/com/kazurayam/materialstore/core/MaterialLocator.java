package com.kazurayam.materialstore.core;

import java.util.Objects;

public class MaterialLocator {

    public static final MaterialLocator NULL_OBJECT = new MaterialLocator(Material.NULL_OBJECT);

    private final JobTimestamp jobTimestamp;
    private final ID id;

    public MaterialLocator(Material material) {
        Objects.requireNonNull(material);
        this.jobTimestamp = material.getJobTimestamp();
        this.id = material.getID();
    }

    public MaterialLocator(JobTimestamp jobTimestamp, ID id) {
        Objects.requireNonNull(jobTimestamp);
        Objects.requireNonNull(id);
        this.jobTimestamp = jobTimestamp;
        this.id = id;
    }

    /*
     * parse a string like "20221023_144115/07730aaa0c4992dcddb58ef5281faec082c8a8ee"
     * to return a MaterialReference object of which JobTimestamp is "20221023_144115"
     * and of which ID is "07730aaa0c4992dcddb58ef5281faec082c8a8ee"
     *
     * @param loc
     * @return
     */
    public static MaterialLocator parse(String loc) {
        Objects.requireNonNull(loc);
        if (!loc.contains("/")) {
            throw new IllegalArgumentException(
                    String.format("loc=\"%s\" does not have a '/' character", loc));
        }
        JobTimestamp jobTimestamp = new JobTimestamp(loc.substring(0, loc.indexOf("/")));
        ID id = new ID(loc.substring(loc.indexOf("/") + 1));
        return new MaterialLocator(jobTimestamp, id);
    }

    public JobTimestamp getJobTimestamp() {
        return this.jobTimestamp;
    }

    public ID getID() {
        return this.id;
    }

    public String toString() {
        return jobTimestamp.toString() + "/" + id.toString();
    }
}
