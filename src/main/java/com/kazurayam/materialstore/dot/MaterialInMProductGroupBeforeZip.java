package com.kazurayam.materialstore.dot;

import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.reduce.MProductGroup;

public class MaterialInMProductGroupBeforeZip implements MNode {

    private final MProductGroup mProductGroup;
    private final Material material;

    private MaterialList materialList;
    private Side side;

    public MaterialInMProductGroupBeforeZip(MProductGroup mProductGroup,
                                            Material material)
            throws MaterialstoreException {
        this.mProductGroup = mProductGroup;
        this.material = material;
        // look up the material inside the MProductGroup.Builder instance
        // to identify in which materialList the material is contained
        if (mProductGroup.getMaterialListLeft().contains(material)) {
            this.materialList = mProductGroup.getMaterialListLeft();
            this.side = Side.L;
        } else if (mProductGroup.getMaterialListRight().contains(material)) {
            this.materialList = mProductGroup.getMaterialListRight();
            this.side = Side.R;
        } else {
            throw new MaterialstoreException("material("
                    + material.getDescription() + ") if not found in the MProductGroup instance");
        }
    }

    @Override
    public MNodeId getMNodeId() {
        return new MNodeId("MPGBZ" + mProductGroup.getShortId()
                + new MaterialInMaterialList(materialList, material).getMNodeId()
                + side.toString());
    }
}
