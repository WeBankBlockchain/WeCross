package com.webank.wecross.routine.htlc;

import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.TransactionResponse;
import com.webank.wecross.stub.VerifiedTransaction;
import java.util.Arrays;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VerifyData {
    private Logger logger = LoggerFactory.getLogger(VerifyData.class);

    private long blockNumber;
    private String transactionHash;
    private String method;
    private String[] args;
    private String[] result;

    public VerifyData(
            long blockNumber,
            String transactionHash,
            String method,
            String[] args,
            String[] result) {
        this.blockNumber = blockNumber;
        this.transactionHash = transactionHash;
        this.method = method;
        this.args = args;
        this.result = result;
    }

    public boolean verify(VerifiedTransaction transaction) {
        if (transaction == null) {
            logger.error("verify transaction failed, transaction: null, verifyData: {}", this);
            return false;
        }

        if (logger.isDebugEnabled()) {
            logger.debug(transaction.toString());
            logger.debug(this.toString());
        }

        TransactionRequest request = transaction.getTransactionRequest();
        TransactionResponse response = transaction.getTransactionResponse();
        if (Objects.isNull(request) || Objects.isNull(response)) {
            logger.error("verify transaction failed, detail:\n{}\n{}", transaction, this);
            return false;
        }

        boolean isEqual =
                getBlockNumber() == transaction.getBlockNumber()
                        && getTransactionHash().equals(transaction.getTransactionHash())
                        && getMethod().equals(request.getMethod())
                        && Arrays.equals(getArgs(), request.getArgs())
                        && Arrays.equals(getResult(), response.getResult());
        if (!isEqual) {
            logger.error("verify transaction failed, detail:\n{}\n{}", transaction, this);
        }
        return isEqual;
    }

    @Override
    public String toString() {
        return "VerifyData{"
                + "blockNumber="
                + blockNumber
                + ", transactionHash='"
                + transactionHash
                + '\''
                + ", method='"
                + method
                + '\''
                + ", args="
                + Arrays.toString(args)
                + ", result="
                + Arrays.toString(result)
                + '}';
    }

    public long getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(long blockNumber) {
        this.blockNumber = blockNumber;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String[] getArgs() {
        return args;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }

    public String[] getResult() {
        return result;
    }

    public void setResult(String[] result) {
        this.result = result;
    }
}
