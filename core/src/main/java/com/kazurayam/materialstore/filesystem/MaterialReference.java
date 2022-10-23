package com.kazurayam.materialstore.filesystem;

import java.util.Objects;

public class MaterialReference {

    private JobTimestamp jobTimestamp;
    private ID id;

    public MaterialReference(Material material) {
        Objects.requireNonNull(material);
        this.jobTimestamp = material.getJobTimestamp();
        this.id = material.getID();
    }

    /**
     * parse a string like "20221023_144115/07730aaa0c4992dcddb58ef5281faec082c8a8ee"
     * to return a MaterialReference object of which JobTimestamp is "20221023_144115"
     * and of which ID is "07730aaa0c4992dcddb58ef5281faec082c8a8ee"
     *
     * @param mref
     * @return
     */
    public static MaterialReference parse(String mref) {
        throw new RuntimeException("TODO");
    }
}
