package com.kazurayam.materialstore.dot;

import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.reduce.MProductGroup;
import com.kazurayam.materialstore.reduce.zipper.MaterialProduct;

import java.util.Objects;

public class GraphNodeIdResolver {

    public static GraphNodeId getGraphNodeId(Material material) {
        Objects.requireNonNull(material);
        return new GraphNodeId("M" + material.getDescriptionSignature());
    }

    public static GraphNodeId getGraphNodeId(MaterialList materialList, Material material) {
        Objects.requireNonNull(materialList);
        Objects.requireNonNull(material);
        MaterialSolo materialSolo = new MaterialSolo(material);
        return new GraphNodeId("ML" + materialList.getShortId()
                + "_"
                + materialSolo.getGraphNodeId());
    }

    public static GraphNodeId getGraphNodeId(MaterialProduct materialProduct, Material material)
            throws MaterialstoreException {
        Objects.requireNonNull(materialProduct);
        Objects.requireNonNull(material);
        Side side;
        if (materialProduct.getLeft() == material) {
            side = Side.L;
        } else if (materialProduct.getRight() == material) {
            side = Side.R;
        } else {
            throw new MaterialstoreException("material(" + material.getShortId()
                    + ") is not contained in this MaterialProduct");
        }
        return new GraphNodeId("MP"
                + materialProduct.getShortId()
                + "_"
                + new MaterialSolo(material).getGraphNodeId()
                + "_"
                + side.toString());
    }

    public static GraphNodeId getGraphNodeId(MProductGroup mProductGroup, Material material) {
        throw new RuntimeException("TODO");
    }

    public static GraphNodeId getGraphNodeIdBeforeZIP(MProductGroup mProductGroup, Material material) {
        throw new RuntimeException("TODO");
    }

    /**
     * hidden constructor
     */
    private GraphNodeIdResolver() {}

}
