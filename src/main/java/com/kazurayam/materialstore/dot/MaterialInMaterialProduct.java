package com.kazurayam.materialstore.dot;

import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.reduce.MaterialProduct;

public class MaterialInMaterialProduct implements GraphNode {

    private final MaterialProduct materialProduct;
    private final Side side;
    private final Material material;

    public MaterialInMaterialProduct(MaterialProduct materialProduct,
                                     Material material)
            throws MaterialstoreException {
        this.materialProduct = materialProduct;
        this.material = material;
        if (materialProduct.getLeft() == material) {
            this.side = Side.L;
        } else if (materialProduct.getRight() == material) {
            this.side = Side.R;
        } else {
            throw new MaterialstoreException("material(" + material.getShortId()
                    + ") is not contained in this MaterialProduct");
        }
    }

    @Override
    public GraphNodeId getGraphNodeId() {
        return new GraphNodeId("MP" + materialProduct.getShortId()
                + new MaterialSolo(material).getGraphNodeId()
                + side.toString());
    }
}