package com.webank.wecross.proof;

import java.util.List;
import org.fisco.bcos.channel.client.Merkle;
import org.fisco.bcos.web3j.protocol.core.methods.response.MerkleProofUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MerkleProof extends PathProof {
    private Logger logger = LoggerFactory.getLogger(MerkleProof.class);

    private String root;
    private List<MerkleProofUnit> path;
    private LeafProof leaf;

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public List<MerkleProofUnit> getPath() {
        return path;
    }

    public void setPath(List<MerkleProofUnit> path) {
        this.path = path;
    }

    @Override
    public boolean verifyRoot(RootProof rootProof) {
        return rootProof.verifyRoot(root);
    }

    @Override
    public boolean verify() {
        String proofRoot = Merkle.calculateMerkleRoot(this.path, this.leaf.getProof());
        logger.debug("verify proof, root:{}, proofRoof:{}", this.root, proofRoot);
        return root.equals(proofRoot);
    }

    @Override
    public boolean hasLeaf(String leaf) {
        return this.leaf.verifyLeaf(leaf);
    }

    public void setLeaf(LeafProof leaf) {
        this.leaf = leaf;
    }

    public LeafProof getLeaf() {
        return leaf;
    }
}
