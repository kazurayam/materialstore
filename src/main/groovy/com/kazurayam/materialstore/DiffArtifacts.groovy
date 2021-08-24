package com.kazurayam.materialstore

import java.util.stream.Collectors

final class DiffArtifacts {

    private final List<DiffArtifact> diffArtifacts

    /**
     * leftMaterialList, rightMaterialList, ignoringMetadataKeys
     * --- these are memorized here just for reporting purpose
     * how this DiffArtifacts object was created.
     */
    private MaterialList leftMaterialList = MaterialList.NULL_OBJECT
    private MaterialList rightMaterialList = MaterialList.NULL_OBJECT
    private IgnoringMetadataKeys ignoringMetadataKeys = IgnoringMetadataKeys.NULL_OBJECT

    DiffArtifacts() {
        diffArtifacts = new ArrayList<DiffArtifact>()
    }

    void add(DiffArtifact e) {
        diffArtifacts.add(e)
    }

    DiffArtifact get(int index) {
        return diffArtifacts.get(index)
    }

    int size() {
        return diffArtifacts.size()
    }

    Iterator<DiffArtifact> iterator() {
        return diffArtifacts.iterator()
    }

    int countWarnings(Double criteria) {
        diffArtifacts.stream()
                .filter { DiffArtifact da ->
                    criteria < da.getDiffRatio()
                }
                .collect(Collectors.toList())
                .size()
    }

    void sort() {
        Collections.sort(diffArtifacts)
    }

    //---------------------------------------------------------------
    void setLeftMaterialList(MaterialList materialList) {
        this.leftMaterialList = materialList
    }

    void setRightMaterialList(MaterialList materialList) {
        this.rightMaterialList = materialList
    }

    void setIgnoringMetadataKeys(IgnoringMetadataKeys ignoringMetadataKeys) {
        this.ignoringMetadataKeys = ignoringMetadataKeys
    }

    MaterialList getLeftMaterialList() {
        return this.leftMaterialList
    }

    MaterialList getRightMaterialList() {
        return this.rightMaterialList
    }

    IgnoringMetadataKeys getIgnoringMetadataKeys() {
        return this.ignoringMetadataKeys
    }

    //---------------------------------------------------------------
    @Override
    String toString() {
        StringBuilder sb = new StringBuilder()
        int count = 0
        sb.append("[")
        diffArtifacts.each {DiffArtifact da ->
            if (count > 0) sb.append(",")
            sb.append(da.toString())
            count += 1
        }
        sb.append("]")
        return sb.toString()
    }
}
