package com.webank.wecross.stub;

import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;

public interface Driver {
    interface Callback {
        void onTransactionResponse(
                TransactionException transactionException, TransactionResponse transactionResponse);
    }

    /**
     * Decode an encoded transaction request binary data.
     *
     * @param request the encoded transaction request binary data
     * @return TransactionRequest
     */
    ImmutablePair<Boolean, TransactionRequest> decodeTransactionRequest(Request request);

    /**
     * get resources name
     *
     * @return resources
     */
    List<ResourceInfo> getResources(Connection connection);

    /**
     * Async Call the interface of contract or chaincode Just fake async for compatibility, you need
     * to override this function
     *
     * @param context
     * @param request the transaction request
     * @param byProxy
     * @param connection the connection of a chain
     * @param callback the callback class for async call
     * @return the transaction response
     */
    void asyncCall(
            TransactionContext context,
            TransactionRequest request,
            boolean byProxy,
            Connection connection,
            Driver.Callback callback);

    /**
     * Async transaction the interface of contract or chaincode Just fake async for compatibility,
     * you need to override this function
     *
     * @param request the transaction request
     * @param connection the connection of a chain
     * @param callback the callback class for async sendTransaction
     * @return the transaction response
     */
    void asyncSendTransaction(
            TransactionContext context,
            TransactionRequest request,
            boolean byProxy,
            Connection connection,
            Driver.Callback callback);

    /**
     * Get block number
     *
     * @return block number
     */
    interface GetBlockNumberCallback {
        void onResponse(Exception e, long blockNumber);
    }

    void asyncGetBlockNumber(Connection connection, GetBlockNumberCallback callback);

    /**
     * Get block
     *
     * @return Block
     */
    interface GetBlockCallback {
        void onResponse(Exception e, Block block);
    }

    void asyncGetBlock(
            long blockNumber, boolean onlyHeader, Connection connection, GetBlockCallback callback);

    interface GetTransactionCallback {
        void onResponse(Exception e, Transaction transaction);
    }

    /**
     * Get verified transaction info of the Chain
     *
     * @param transactionHash
     * @param blockNumber
     * @param blockManager
     * @param isVerified whether it needs to be verified
     * @param connection
     * @return null if the transaction has not been verified
     */
    void asyncGetTransaction(
            String transactionHash,
            long blockNumber,
            BlockManager blockManager,
            boolean isVerified,
            Connection connection,
            GetTransactionCallback callback);

    interface CustomCommandCallback {
        void onResponse(Exception error, Object response);
    }

    /**
     * Custom command
     *
     * @param path
     * @param args
     * @param account
     * @param blockManager
     * @param connection
     * @param callback
     */
    void asyncCustomCommand(
            String command,
            Path path,
            Object[] args,
            Account account,
            BlockManager blockManager,
            Connection connection,
            CustomCommandCallback callback);

    /**
     * @param account
     * @param message
     * @return signBytes
     */
    byte[] accountSign(Account account, byte[] message);

    /**
     * @param identity: Chain account identity
     * @param signBytes
     * @param message
     * @return success or failed
     */
    boolean accountVerify(String identity, byte[] signBytes, byte[] message);
}
