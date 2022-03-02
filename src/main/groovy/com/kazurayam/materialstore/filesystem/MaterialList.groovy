package com.kazurayam.materialstore.filesystem

import com.kazurayam.materialstore.metadata.Metadata
import com.kazurayam.materialstore.metadata.QueryOnMetadata

final class MaterialList {

    public static final NULL_OBJECT = new MaterialList(JobTimestamp.NULL_OBJECT, QueryOnMetadata.NULL_OBJECT, FileType.NULL_OBJECT)

    private JobTimestamp jobTimestamp

    private QueryOnMetadata query

    private FileType fileType

    private List<Material> materialList

    MaterialList(JobTimestamp jobTimestamp, QueryOnMetadata query,
                 FileType fileType) {
        Objects.requireNonNull(jobTimestamp)
        Objects.requireNonNull(query)
        Objects.requireNonNull(fileType)
        this.jobTimestamp = jobTimestamp
        this.query = query
        this.fileType = fileType
        //
        this.materialList = new ArrayList<Material>()
    }

    /**
     * Deep-copy constructor
     * @param source
     */
    MaterialList(MaterialList source) {
        this.jobTimestamp = source.jobTimestamp         // JobTimestamp is immutable
        this.query = source.queryOnMetadata   // QueryOnMetadata is immutable
        this.fileType = source.fileType                 // FileType is immutable
        this.materialList = new ArrayList<>(source.materialList)  // Material is immutable
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

    boolean containsSimilarMetadataAs(Material material) {
        Metadata baseMetadata = material.getMetadata()
        for (Material targetMaterial : materialList) {
            Metadata targetMetadata = targetMaterial.getMetadata()
            if (baseMetadata == targetMetadata) {
                return true
            }
        }
        return false
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

    QueryOnMetadata getQueryOnMetadata() {
        return this.query
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
        sb2.append('''"queryOnMetadata":''')
        sb2.append(this.query.toString())
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
