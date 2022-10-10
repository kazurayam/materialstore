package com.kazurayam.materialstore.filesystem;

import com.kazurayam.materialstore.filesystem.metadata.OrdinalDescription;
import com.kazurayam.materialstore.filesystem.metadata.SortKeys;

import java.util.Comparator;
import java.util.Objects;

public class MaterialComparatorByOrdinalDescription implements Comparator<Material> {

    private final SortKeys sortKeys;

    public MaterialComparatorByOrdinalDescription() {
        this(new SortKeys());
    }

    public MaterialComparatorByOrdinalDescription(SortKeys sortKeys) {
        this.sortKeys = sortKeys;
    }

    @Override
    public int compare(Material m1, Material m2) {
        Objects.requireNonNull(m1);
        Objects.requireNonNull(m2);
        OrdinalDescription od1 = m1.getMetadata().getOrdinalDescription(sortKeys);
        OrdinalDescription od2 = m2.getMetadata().getOrdinalDescription(sortKeys);
        return od1.toString().compareTo(od2.toString());
    }
}
