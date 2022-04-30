package com.kazurayam.materialstore.dot;

import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.reduce.MProductGroup;

public class MaterialInMProductGroupBuilder implements MaterialNode {

    private final MProductGroup.Builder mpgBuilder;
    private final Material material;

    private MaterialList materialList;
    private Side side;

    public MaterialInMProductGroupBuilder(MProductGroup.Builder mpgBuilder,
                                          Material material)
            throws MaterialstoreException {
        this.mpgBuilder = mpgBuilder;
        this.material = material;
        // look up the material inside the MProductGroup.Builder instance
        // to identify in which materialList the material is contained
        if (mpgBuilder.getMaterialList0().contains(material)) {
            this.materialList = mpgBuilder.getMaterialList0();
            this.side = Side.L;
        } else if (mpgBuilder.getMaterialList1().contains(material)) {
            this.materialList = mpgBuilder.getMaterialList1();
            this.side = Side.R;
        } else {
            throw new MaterialstoreException("material("
                    + material.getDescription() + ") if not found in the Builder instance");
        }
    }

    @Override
    public NodeId getNodeId() {
        return new NodeId("MPGB" + mpgBuilder.getShortId()
                + new MaterialInMaterialList(materialList, material).getNodeId()
                + side.toString());
    }
}
