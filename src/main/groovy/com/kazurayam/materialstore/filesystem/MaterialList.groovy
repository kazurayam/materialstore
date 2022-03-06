package com.kazurayam.materialstore.filesystem

import com.kazurayam.materialstore.metadata.Metadata
import com.kazurayam.materialstore.metadata.QueryOnMetadata
import org.slf4j.Logger
import org.slf4j.LoggerFactory

final class MaterialList {

    private static Logger logger = LoggerFactory.getLogger(MaterialList.class.getName())

    public static final NULL_OBJECT =
            new MaterialList(JobName.NULL_OBJECT, JobTimestamp.NULL_OBJECT,
                    QueryOnMetadata.NULL_OBJECT, FileType.NULL_OBJECT)

    private JobName jobName

    private JobTimestamp jobTimestamp

    private QueryOnMetadata query

    private FileType fileType

    private List<Material> materialList

    MaterialList(JobName jobName, JobTimestamp jobTimestamp,
                 QueryOnMetadata query, FileType fileType) {
        Objects.requireNonNull(jobName)
        Objects.requireNonNull(jobTimestamp)
        Objects.requireNonNull(query)
        Objects.requireNonNull(fileType)
        this.jobName = jobName
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
        this.jobName = source.jobName
        this.jobTimestamp = source.jobTimestamp         // JobTimestamp is immutable
        this.query = source.queryOnMetadata   // QueryOnMetadata is immutable
        this.fileType = source.fileType                 // FileType is immutable
        this.materialList = new ArrayList<>(source.materialList)  // Material is immutable
    }

    //--------------- List-like methods -----------

    void add(Material material) {
        materialList.add(material)
    }

    void add(List<Material> list) {
        list.each {material ->
            materialList.add(material)
        }
    }

    Material get(int index) {
        return materialList.get(index)
    }

    boolean contains(Material material) {
        return materialList.contains(material)
    }

    boolean containsMaterialsSimilarTo(Material baseMaterial) {
        List<Material> found = findMaterialsSimilarTo(baseMaterial)
        return (found.size() > 0)
    }

    List<Material> findMaterialsSimilarTo(Material baseMaterial) {
        List<Material> list = new ArrayList<>()
        for (Material targetMaterial : materialList) {
            boolean similar = targetMaterial.isSimilar(baseMaterial)
            if (similar) {
                logger.debug(String.format(
                        "[findMaterialsSimilarTo] target=%s is similar to base=%s",
                        targetMaterial.getShortId(), baseMaterial.getShortId()))

                list.add(targetMaterial)

            } else {
                logger.debug(String.format(
                        "[findMaterialsSimilarTo] target=%s is NOT similar to base=%s",
                        targetMaterial.getShortId(), baseMaterial.getShortId()))
            }
        }
        logger.debug(String.format("[findMaterialsSimilarTo] list.size()=%d", list.size()))
        return list
    }

    int countMaterialsWithIdStartingWith(String idStarter) {
        int count = 0
        for (Material material : materialList) {
            if (material.getIndexEntry().getID().getSha1().startsWith(idStarter)) {
                count += 1
            }
        }
        return count
    }

    int size() {
        return materialList.size()
    }

    Iterator<Material> iterator() {
        return materialList.iterator()
    }

    // --------------- unique method ------------
    JobName getJobName() {
        return this.jobName
    }

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
        sb2.append("\"jobName\":\"" + jobName.toString() + "\"")
        sb2.append(",")
        sb2.append("\"jobTimestamp\":\"" + jobTimestamp.toString() + "\"")
        sb2.append(",")
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
