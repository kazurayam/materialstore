package com.kazurayam.materialstore

import groovy.json.JsonOutput

final class MaterialList {

    private JobTimestamp jobTimestamp

    private MetadataPattern metadataPattern

    private FileType fileType

    private List<Material> materialList

    MaterialList(JobTimestamp jobTimestamp, MetadataPattern metadataPattern, FileType fileType) {
        this.jobTimestamp = jobTimestamp
        this.metadataPattern = metadataPattern
        this.fileType = fileType
        this.materialList = new ArrayList<Material>()
    }

    //--------------- List-like methods -----------

    void add(Material material) {
        materialList.add(material)
    }

    Material get(int index) {
        return materialList.get(index)
    }

    boolean contains(Material material) {
        return materialList.contains(material)
    }

    int size() {
        return materialList.size()
    }

    Iterator<Material> iterator() {
        return materialList.iterator()
    }

    // --------------- unique method ------------
    JobTimestamp getJobTimestamp() {
        return this.jobTimestamp
    }

    MetadataPattern getMetadataPattern() {
        return this.metadataPattern
    }

    FileType getFileType() {
        return this.fileType
    }

    //---------------- java.lang.Object -----------
    @Override
    String toString() {
        StringBuilder sb = new StringBuilder()
        int count = 0
        sb.append("[")
        this.materialList.each {material ->
            if (count > 0) {
                sb.append(",")
            }
            sb.append(material.toString())
            count += 1
        }
        sb.append("]")
        Map m = [
                "metadataPattern": this.metadataPattern.toString(),
                "fileType": this.fileType.extension,
                "materialList": sb.toString()
        ]
        return JsonOutput.toJson(m)
    }
}
