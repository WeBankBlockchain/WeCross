package com.webank.wecross.stub.bcos.contract;

import java.math.BigInteger;
import java.util.concurrent.Semaphore;
import org.fisco.bcos.channel.client.TransactionSucCallback;
import org.fisco.bcos.web3j.abi.datatypes.Function;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.tx.Contract;

public class ExecuteTransaction extends Contract {

    public ExecuteTransaction(
            String contractAddress,
            Web3j web3j,
            Credentials credentials,
            BigInteger gasPrice,
            BigInteger gasLimit) {
        super("", contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    public TransactionReceipt send(Function function) throws Exception {
        return executeTransaction(function);
    }

    public void asyncSend(Function function, TransactionSucCallback callback) {
        asyncExecuteTransaction(function, callback);
    }

    public TransactionReceipt sendSignedTransaction(String signedTransaction) throws Exception {

        Callback callback = new Callback();
        try {
            transactionManager.sendTransaction(signedTransaction, callback);

            callback.semaphore.acquire(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return callback.receipt;
    }

    class Callback extends TransactionSucCallback {
        Callback() {
            try {
                semaphore.acquire(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Override
        public void onResponse(TransactionReceipt receipt) {
            this.receipt = receipt;
            semaphore.release();
        }

        public TransactionReceipt receipt;
        public Semaphore semaphore = new Semaphore(1, true);
    };
}
