package com.kazurayam.materialstore.dot;

import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.reduce.MaterialProduct;

public class MProductSubgraph {

    private MaterialProduct materialProduct;
    private MNodeId left;
    private MNodeId right;

    public MProductSubgraph(MaterialProduct mp) throws MaterialstoreException {
        this.materialProduct = mp;
        this.left = new MaterialInMaterialProduct(mp, mp.getLeft()).getMNodeId();
        this.right = new MaterialInMaterialProduct(mp, mp.getRight()).getMNodeId();
    }

    public MNodeId getLeft() {
        return left;
    }

    public MNodeId getRight() {
        return right;
    }

    public MaterialProduct getMaterialProduct() {
        return materialProduct;
    }


}
