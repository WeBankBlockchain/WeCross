package com.webank.wecross.proof;

import java.util.Arrays;
import java.util.List;
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
        // Verify leaf
        if (!this.leaf.verifyProof()) {
            return false;
        }

        // Verify path
        String proofRoot = calculateMerkleRoot(this.path, this.leaf.getProof());
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

    @Override
    public void setProofTools(ProofTools proofTools) {
        super.setProofTools(proofTools);
        leaf.setProofTools(proofTools);
    }

    public String calculateMerkleRoot(List<MerkleProofUnit> merkleProofUnits, String hash) {
        if (merkleProofUnits == null) {
            return hash;
        }
        String result = hash;
        for (MerkleProofUnit merkleProofUnit : merkleProofUnits) {
            String left = splicing(merkleProofUnit.getLeft());
            String right = splicing(merkleProofUnit.getRight());
            String input = splicing("0x", left, result.substring(2), right);
            result = getProofTools().hash(input);
        }
        return result;
    }

    private static String splicing(List<String> stringList) {
        StringBuilder result = new StringBuilder();
        for (String eachString : stringList) {
            result.append(eachString);
        }
        return result.toString();
    }

    private static String splicing(String... stringList) {
        return splicing(Arrays.<String>asList(stringList));
    }
}
