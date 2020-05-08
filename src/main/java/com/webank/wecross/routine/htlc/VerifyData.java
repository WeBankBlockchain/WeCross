package com.webank.wecross.routine.htlc;

import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.VerifiedTransaction;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VerifyData {
    private Logger logger = LoggerFactory.getLogger(VerifyData.class);

    private long blockNumber;
    private String transactionHash;
    private String realAddress;
    private String method;
    private String[] args;
    private String[] result;

    public VerifyData(
            long blockNumber,
            String transactionHash,
            String realAddress,
            String method,
            String[] args,
            String[] result) {
        this.blockNumber = blockNumber;
        this.transactionHash = transactionHash;
        this.realAddress = realAddress;
        this.method = method;
        this.args = args;
        this.result = result;
    }

    public boolean verify(VerifiedTransaction transaction) {
        if (transaction == null) {
            logger.error("verify transaction failed, transaction: null, verifyData: {}", this);
            return false;
        }

        logger.debug(transaction.toString());
        logger.debug(this.toString());

        TransactionRequest request = transaction.getTransactionRequest();
        boolean isEqual =
                getBlockNumber() == transaction.getBlockNumber()
                        && getTransactionHash().equals(transaction.getTransactionHash())
                        && getRealAddress().equals(transaction.getRealAddress())
                        //                && getMethod().verify(request.getMethod())
                        && Arrays.equals(getArgs(), request.getArgs())
                        && Arrays.equals(
                                getResult(), transaction.getTransactionResponse().getResult());
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
                + ", realAddress='"
                + realAddress
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

    public String getRealAddress() {
        return realAddress;
    }

    public void setRealAddress(String realAddress) {
        this.realAddress = realAddress;
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
