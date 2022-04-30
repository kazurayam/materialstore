package com.kazurayam.materialstore.dot;

import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.MaterialList;

public class MaterialInMaterialList implements MaterialNode {

    private final Material material;
    private final MaterialList materialList;

    public MaterialInMaterialList(MaterialList materialList, Material material) {
        this.materialList = materialList;
        this.material = material;
    }

    @Override
    public NodeId getNodeId() {
        MaterialSolo materialSolo = new MaterialSolo(material);
        return new NodeId("ML" + materialList.getShortId()
                + materialSolo.getNodeId());
    }
}
