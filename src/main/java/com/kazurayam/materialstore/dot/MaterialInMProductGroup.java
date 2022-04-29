package com.kazurayam.materialstore.dot;

import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.reduce.MProductGroup;
import com.kazurayam.materialstore.reduce.MaterialProduct;

public class MaterialInMProductGroup implements MaterialAsNode {

    private final Material material;
    private final MProductGroup mProductGroup;

    private MaterialProduct materialProduct = null;
    private Side side = null;

    public MaterialInMProductGroup(MProductGroup mProductGroup,
                                   Material material)
            throws MaterialstoreException {
        this.material = material;
        this.mProductGroup = mProductGroup;
        //
        for (MaterialProduct mp : mProductGroup) {
            if (mp.contains(material)) {
                this.materialProduct = mp;
                this.side =
                        (mp.containsMaterialAt(material) == -1) ?
                                Side.L : Side.R;
            }
        }
        if (this.materialProduct == null) {
            throw new MaterialstoreException("material("
                    + this.material.getDescription() + ") is not contained");
        }
    }

    @Override
    public String getNodeId() throws MaterialstoreException {
        return "MPG" + mProductGroup.getShortId()
                + new MaterialInMaterialProduct(materialProduct, material).getNodeId()
                + side.toString();
    }
}
