package com.webank.wecross.stub;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionResponse {
    private Logger logger = LoggerFactory.getLogger(TransactionResponse.class);

    private Integer errorCode;
    private String errorMessage;
    private String hash;
    private List<String> extraHashes;
    private long blockNumber;
    private String[] result;

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

    public long getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(long blockNumber) {
        this.blockNumber = blockNumber;
    }

    public String[] getResult() {
        return result;
    }

    public void setResult(String[] result) {
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

    @Override
    public String toString() {
        return "TransactionResponse{"
                + "errorCode="
                + errorCode
                + ", errorMessage='"
                + errorMessage
                + '\''
                + ", hash='"
                + hash
                + '\''
                + ", extraHashes="
                + extraHashes
                + ", result="
                + Arrays.toString(result)
                + '\''
                + '\''
                + '}';
    }
}
