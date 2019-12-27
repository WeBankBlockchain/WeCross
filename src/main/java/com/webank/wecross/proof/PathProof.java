package com.webank.wecross.proof;

public abstract class PathProof extends Proof {

    public abstract boolean verifyRoot(RootProof rootProof);

    public abstract boolean verify();

    public abstract boolean hasLeaf(String leaf);
}
