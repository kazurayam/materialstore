package com.kazurayam.materialstore.reduce.zipper;

import com.kazurayam.materialstore.filesystem.metadata.QueryIdentification;
import com.kazurayam.materialstore.filesystem.metadata.SortKeys;

import java.util.Comparator;
import java.util.Objects;

public class MaterialProductComparator implements Comparator<MaterialProduct> {

    private final SortKeys sortKeys;

    public MaterialProductComparator() { this(new SortKeys()); }

    public MaterialProductComparator(SortKeys sortKeys) { this.sortKeys = sortKeys; }

    @Override
    public int compare(MaterialProduct mp1, MaterialProduct mp2) {
        Objects.requireNonNull(mp1);
        Objects.requireNonNull(mp2);
        QueryIdentification qi1 = mp1.getQueryIdentification(sortKeys);
        QueryIdentification qi2 = mp2.getQueryIdentification(sortKeys);
        return qi1.toString().compareTo(qi2.toString());
    }
}
