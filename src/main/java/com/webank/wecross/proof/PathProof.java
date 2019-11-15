package com.webank.wecross.proof;

public abstract class PathProof {
    public abstract boolean verifyRoot(RootProof rootProof);

    public abstract boolean verify();

    public abstract boolean hasLeaf(String leaf);
}
