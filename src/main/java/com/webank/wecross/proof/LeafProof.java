package com.webank.wecross.proof;

import java.math.BigInteger;
import org.fisco.bcos.web3j.rlp.RlpEncoder;
import org.fisco.bcos.web3j.rlp.RlpString;
import org.fisco.bcos.web3j.utils.Numeric;

public class LeafProof extends Proof {
    protected String index;
    protected String leaf;
    protected String proof;

    public boolean verifyLeaf(String leaf) {
        return this.leaf.equals(leaf);
    }

    public boolean verifyProof() {
        BigInteger indexValue = Numeric.toBigInt(index);
        byte[] byteIndex = RlpEncoder.encode(RlpString.create(indexValue));
        String expectedProof = Numeric.toHexString(byteIndex) + leaf.substring(2);
        return expectedProof.equals(proof);
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getLeaf() {
        return leaf;
    }

    public void setLeaf(String leaf) {
        this.leaf = leaf;
    }

    public String getProof() {
        return proof;
    }

    public void setProof(String proof) {
        this.proof = proof;
    }
}
