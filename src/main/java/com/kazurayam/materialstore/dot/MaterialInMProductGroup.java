package com.kazurayam.materialstore.dot;

import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.reduce.MProductGroup;
import com.kazurayam.materialstore.reduce.zipper.MaterialProduct;

public class MaterialInMProductGroup implements GraphNode {

    private final Material material;
    private final MProductGroup mProductGroup;

    private MaterialProduct materialProduct = null;

    public MaterialInMProductGroup(MProductGroup mProductGroup, Material material)
            throws MaterialstoreException {
        this.material = material;
        this.mProductGroup = mProductGroup;
        for (MaterialProduct mp : mProductGroup) {
            if (mp.contains(material)) {
                this.materialProduct = mp;
            }
        }
        if (this.materialProduct == null) {
            throw new MaterialstoreException("material("
                    + this.material.getDescription() + ") is not contained");
        }
    }

    @Override
    public GraphNodeId getGraphNodeId() throws MaterialstoreException {
        return new GraphNodeId("MPG"
                + mProductGroup.getShortId()
                + new MaterialInMaterialProduct(materialProduct, material).getGraphNodeId());
    }
}
