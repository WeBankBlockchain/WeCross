package com.webank.wecross.stub;

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
