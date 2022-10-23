package com.kazurayam.materialstore.dot;

import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.reduce.MaterialProductGroup;
import com.kazurayam.materialstore.reduce.zipper.MaterialProduct;

import java.util.Objects;

public class GraphNodeIdResolver {

    public static GraphNodeId resolveIdOfMaterialSolo(Material material) {
        Objects.requireNonNull(material);
        return new GraphNodeId("M" + material.getDescriptionSignature());
    }

    public static GraphNodeId resolveIdOfMaterialInMaterialList(MaterialList materialList, Material material) {
        Objects.requireNonNull(materialList);
        Objects.requireNonNull(material);
        return new GraphNodeId("ML" + materialList.getShortID()
                + "_"
                + resolveIdOfMaterialSolo(material));
    }

    public static GraphNodeId resolveIdOfMaterialInMaterialProduct(MaterialProduct materialProduct, Role role)
            throws MaterialstoreException {
        Objects.requireNonNull(materialProduct);
        Objects.requireNonNull(role);
        Material material;
        if (role.equals(Role.L)) {
            material = materialProduct.getLeft();
        } else if (role.equals(Role.R)) {
            material = materialProduct.getRight();
        } else if (role.equals(Role.D)) {
            material = materialProduct.getDiff();
        } else {
            throw new MaterialstoreException("unexpected role is specified: " + role);
        }
        return new GraphNodeId("MP"
                + materialProduct.getShortID()
                + "_"
                + resolveIdOfMaterialSolo(material)
                + "_"
                + role.toString());
    }

    public static GraphNodeId resolveIdOfQueryInMaterialProduct(MaterialProduct materialProduct) {
        Objects.requireNonNull(materialProduct);
        return new GraphNodeId("MP" + materialProduct.getShortID() + "_Q");
    }

    public static GraphNodeId resolveIdOfMaterialInMProductGroup(MaterialProductGroup mProductGroup, Material material)
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
                + mProductGroup.getShortID()
                + "_"
                + resolveIdOfMaterialSolo(material)
        );
    }

    public static GraphNodeId resolveIdOfMaterialInMProductGroupBeforeZIP(MaterialProductGroup mProductGroup, Material material)
            throws MaterialstoreException {
        Objects.requireNonNull(mProductGroup);
        Objects.requireNonNull(material);
        MaterialList materialList = null;
        // look up the material inside the MProductGroup.Builder instance
        // to identify in which materialList the material is contained
        if (mProductGroup.getMaterialListLeft().contains(material)) {
            materialList = mProductGroup.getMaterialListLeft();
        } else if (mProductGroup.getMaterialListRight().contains(material)) {
            materialList = mProductGroup.getMaterialListRight();
        } else {
            throw new MaterialstoreException("material("
                    + material.getDescription() + ") if not found in the MProductGroup instance");
        }
        return new GraphNodeId("MPGBZ" + mProductGroup.getShortID()
                + "_"
                + resolveIdOfMaterialInMaterialList(materialList, material));
    }

    /**
     * hidden constructor
     */
    private GraphNodeIdResolver() {}

}
