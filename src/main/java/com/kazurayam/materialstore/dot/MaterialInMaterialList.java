package com.kazurayam.materialstore.dot;

import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.MaterialList;

public class MaterialInMaterialList implements GraphNode {

    private final Material material;
    private final MaterialList materialList;

    public MaterialInMaterialList(MaterialList materialList, Material material) {
        this.materialList = materialList;
        this.material = material;
    }

    @Override
    public GraphNodeId getGraphNodeId() {
        MaterialSolo materialSolo = new MaterialSolo(material);
        return new GraphNodeId("ML" + materialList.getShortId()
                + materialSolo.getGraphNodeId());
    }
}
