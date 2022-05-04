package com.kazurayam.materialstore.dot;

import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.util.StringUtils;

public class MaterialSolo implements GraphNode {

    private final Material material;

    public MaterialSolo(Material material) {
        this.material = material;
    }

    @Override
    public GraphNodeId getGraphNodeId() {
        return new GraphNodeId("M" + material.getDescriptionSignature());
    }

}
