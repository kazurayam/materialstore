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
        return new GraphNodeId("ML" + materialList.getShortId()
                + "_"
                + getGraphNodeId(material));
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
                + getGraphNodeId(material)
                + side.toString());
    }

    public static GraphNodeId getGraphNodeId(MProductGroup mProductGroup, Material material)
            throws MaterialstoreException {
        MaterialProduct materialProduct = null;
        for (MaterialProduct mp : mProductGroup) {
            if (mp.contains(material)) {
                materialProduct = mp;
            }
        }
        if (materialProduct == null) {
            throw new MaterialstoreException("material("
                    + material.getDescription() + ") is not contained");
        }
        return new GraphNodeId("MPG"
                + mProductGroup.getShortId()
                + "_"
                + getGraphNodeId(materialProduct, material)
        );
    }

    public static GraphNodeId getGraphNodeIdBeforeZIP(MProductGroup mProductGroup, Material material)
            throws MaterialstoreException {
        Objects.requireNonNull(mProductGroup);
        Objects.requireNonNull(material);
        MaterialList materialList = null;
        Side side = null;
        // look up the material inside the MProductGroup.Builder instance
        // to identify in which materialList the material is contained
        if (mProductGroup.getMaterialListLeft().contains(material)) {
            materialList = mProductGroup.getMaterialListLeft();
            side = Side.L;
        } else if (mProductGroup.getMaterialListRight().contains(material)) {
            materialList = mProductGroup.getMaterialListRight();
            side = Side.R;
        } else {
            throw new MaterialstoreException("material("
                    + material.getDescription() + ") if not found in the MProductGroup instance");
        }
        return new GraphNodeId("MPGBZ" + mProductGroup.getShortId()
                + "_"
                + GraphNodeIdResolver.getGraphNodeId(materialList, material)
                + "_"
                + side.toString());
    }

    /**
     * hidden constructor
     */
    private GraphNodeIdResolver() {}

}
