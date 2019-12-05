package com.webank.wecross.proof;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

public class BlockHeaderProof extends RootProof {
    private BigInteger blockNumber;
    private String hash;
    private Set<String> roots = new HashSet<>();

    @Override
    public boolean verifyRoot(String root) {
        return roots.contains(root);
    }

    public BigInteger getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(BigInteger blockNumber) {
        this.blockNumber = blockNumber;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Set<String> getRoots() {
        return roots;
    }

    public void setRoots(Set<String> roots) {
        this.roots = roots;
    }

    public void addRoot(String root) {
        this.roots.add(root);
    }
}
