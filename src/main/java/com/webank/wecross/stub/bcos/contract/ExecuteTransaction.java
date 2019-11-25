package com.webank.wecross.stub.bcos.contract;

import java.math.BigInteger;
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
}
