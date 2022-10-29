package com.kazurayam.materialstore.core.filesystem;

import com.kazurayam.materialstore.core.filesystem.metadata.MetadataIdentification;

import java.util.Comparator;
import java.util.Objects;

public class MaterialComparatorByMetadataDescription implements Comparator<Material> {

    private final SortKeys sortKeys;

    public MaterialComparatorByMetadataDescription() {
        this(new SortKeys());
    }

    public MaterialComparatorByMetadataDescription(SortKeys sortKeys) {
        this.sortKeys = sortKeys;
    }

    @Override
    public int compare(Material m1, Material m2) {
        Objects.requireNonNull(m1);
        Objects.requireNonNull(m2);
        MetadataIdentification mi1 = m1.getMetadata().getMetadataIdentification(sortKeys);
        MetadataIdentification mi2 = m2.getMetadata().getMetadataIdentification(sortKeys);
        return mi1.toString().compareTo(mi2.toString());
    }
}
