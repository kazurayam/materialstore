package com.kazurayam.materialstore.filesystem

import com.kazurayam.materialstore.metadata.MetadataPattern

final class MaterialList {

    public static final NULL_OBJECT = new MaterialList(JobTimestamp.NULL_OBJECT, MetadataPattern.NULL_OBJECT, FileType.NULL_OBJECT)

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
        //
        StringBuilder sb2 = new StringBuilder()
        sb2.append("{")
        sb2.append('''"metadataPattern":''')
        sb2.append(this.metadataPattern.toString())
        sb2.append(",")
        sb2.append('''"fileType":''')
        sb2.append('''"''')
        sb2.append(this.fileType.getExtension())
        sb2.append('''",''')
        sb2.append('''"metadataList":''')
        sb2.append(sb.toString())
        sb2.append("}")
        return sb2.toString()
    }
}
