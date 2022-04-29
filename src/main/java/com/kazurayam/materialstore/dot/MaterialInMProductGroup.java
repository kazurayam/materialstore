package com.kazurayam.materialstore.dot;

import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.reduce.MProductGroup;
import com.kazurayam.materialstore.reduce.MaterialProduct;

public class MaterialInMProductGroup implements MaterialAsNode {

    private final Material material;
    private final Side side;
    private final MaterialProduct materialProduct;
    private final MProductGroup mProductGroup;

    public MaterialInMProductGroup(MProductGroup mProductGroup,
                                   MaterialProduct materialProduct,
                                   Side side,
                                   Material material) {
        this.material = material;
        this.side = side;
        this.materialProduct = materialProduct;
        this.mProductGroup = mProductGroup;
    }

    @Override
    public String getNodeId() throws MaterialstoreException {
        return "MPG" + mProductGroup.getShortId()
                + new MaterialInMaterialProduct(materialProduct, material).getNodeId()
                + new MaterialSolo(material).getNodeId()
                + side.toString();
    }
}
