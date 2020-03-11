package com.webank.wecross.stub;

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
    private String type = "";
    private String encryptType = "";

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
        // beforeVerify();

        try {
            if (!errorCode.equals(0)) {
                return true; // error no need to verify
            }

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
                                + Arrays.toString(getProofs())
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
                                    + Arrays.toString(getProofs())
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

    public String getEncryptType() {
        return encryptType;
    }

    public void setEncryptType(String encryptType) {
        this.encryptType = encryptType;
    }

    /*
    private void beforeVerify() {
        configProofTools();
    }


    private void configProofTools() {
        ProofTools proofTools = newProofTools();

        if (getBlockHeader() != null) {
            getBlockHeader().setProofTools(proofTools);
        }

        if (getProofs() != null) {
            for (PathProof proof : getProofs()) {
                proof.setProofTools(proofTools);
            }
        }
    }

    private ProofTools newProofTools() {
        ProofTools proofTools = null;
        switch (getType()) {
            case WeCrossType.TRANSACTION_RSP_TYPE_BCOS:
                {
                    switch (getEncryptType()) {
                        case WeCrossType.ENCRYPT_TYPE_GUOMI:
                            proofTools = new BCOSGuomiProofTools();
                            break;

                        case WeCrossType.ENCRYPT_TYPE_NORMAL:
                        default:
                            proofTools = new BCOSProofTools();
                            break;
                    }
                }
                break;

            default:
                break;
        }
        return proofTools;
    }
    */
}
