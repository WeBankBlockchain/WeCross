package com.webank.wecross.restserver.response;

import com.webank.wecross.network.config.ConfigType;
import com.webank.wecross.proof.PathProof;
import com.webank.wecross.proof.ProofConfig;
import com.webank.wecross.proof.RootProof;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionResponse {
    private Logger logger = LoggerFactory.getLogger(TransactionResponse.class);

    @Override
    public String toString() {
        return "TransactionResponse [errorCode="
                + errorCode
                + ", errorMessage="
                + errorMessage
                + ", hash="
                + hash
                + ", result="
                + Arrays.toString(result)
                + "]";
    }

    private Integer errorCode;
    private String errorMessage;
    private String hash;
    private List<String> extraHashes;
    private Object result[];
    private String type = ConfigType.TRANSACTION_RSP_TYPE_NORMAL;

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Object[] getResult() {
        return result;
    }

    public void setResult(Object result[]) {
        this.result = result;
    }

    public RootProof getBlockHeader() {
        return null;
    }

    public PathProof[] getProofs() {
        return new PathProof[] {};
    }

    public boolean verify() {
        try {

            if (!ProofConfig.supportSPV(getType())) {
                return true;
            }

            // Verify leaf: hash (transaction hash)
            boolean hasLeaf = false;
            for (PathProof pathProof : getProofs()) {
                hasLeaf |= pathProof.hasLeaf(hash);
            }
            if (!hasLeaf) {
                throw new Exception(
                        "leaf failed, no path can prove the leaf. paths:"
                                + getProofs().toString()
                                + ", leaf:"
                                + hash);
            }

            // Verify leaf: extraHashes (receipt hash and so on)
            if (extraHashes != null) {
                hasLeaf = false;
                for (String extraHash : extraHashes) {
                    for (PathProof pathProof : getProofs()) {
                        hasLeaf |= pathProof.hasLeaf(extraHash);
                    }
                }
                if (!hasLeaf) {
                    throw new Exception(
                            "leaf failed, no path can prove the leaf. paths:"
                                    + getProofs().toString()
                                    + ", leaf:"
                                    + hash);
                }
            }

            // Verify path
            for (PathProof pathProof : getProofs()) {
                if (!pathProof.verify()) {
                    throw new Exception("path failed, path:" + pathProof);
                }
            }

            // Verify root
            for (PathProof pathProof : getProofs()) {
                if (!pathProof.verifyRoot(getBlockHeader())) {
                    throw new Exception(
                            "root failed, path:" + pathProof + ", blockHeader:" + getBlockHeader());
                }
            }

        } catch (Exception e) {
            logger.warn("Verify " + e);
            return false;
        }

        logger.debug("Verify transaction response success, hash:{}", hash);
        return true;
    }

    public void setExtraHashes(List<String> extraHashes) {
        this.extraHashes = extraHashes;
    }

    public List<String> getExtraHashes() {
        return extraHashes;
    }

    public void addExtraHash(String hash) {
        if (this.extraHashes == null) {
            this.extraHashes = new ArrayList<>();
        }
        this.extraHashes.add(hash);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
