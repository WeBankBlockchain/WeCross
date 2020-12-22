package com.webank.wecross.stub;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TransactionResponse {
    private int errorCode = 0;
    private String message = new String();
    private String hash = new String();
    private List<String> extraHashes;
    private long blockNumber;
    private String[] result;

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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
                + message
                + '\''
                + ", hash='"
                + hash
                + '\''
                + ", extraHashes="
                + extraHashes
                + ", blockNumber="
                + blockNumber
                + ", result="
                + Arrays.toString(result)
                + '}';
    }
}
