package com.kazurayam.materialstore.filesystem;

import com.kazurayam.materialstore.filesystem.metadata.MetadataDescription;
import com.kazurayam.materialstore.filesystem.metadata.SortKeys;

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
        MetadataDescription md1 = m1.getMetadata().getMetadataDescription(sortKeys);
        MetadataDescription md2 = m2.getMetadata().getMetadataDescription(sortKeys);
        return md1.toString().compareTo(md2.toString());
    }
}
