package com.kazurayam.materialstore.dot;

import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.MaterialList;

public class MaterialInMaterialList implements MNode {

    private final Material material;
    private final MaterialList materialList;

    public MaterialInMaterialList(MaterialList materialList, Material material) {
        this.materialList = materialList;
        this.material = material;
    }

    @Override
    public MNodeId getMNodeId() {
        MaterialSolo materialSolo = new MaterialSolo(material);
        return new MNodeId("ML" + materialList.getShortId()
                + materialSolo.getMNodeId());
    }
}
