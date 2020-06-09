package com.webank.wecross.stub;

import com.webank.wecross.stubmanager.BlockHeaderManager;

public interface Driver {
    interface Callback {
        void onTransactionResponse(
                TransactionException transactionException, TransactionResponse transactionResponse);
    }

    /**
     * Decode an encoded transaction request binary data.
     *
     * @param data the encoded transaction request binary data
     * @return TransactionRequest
     */
    public TransactionContext<TransactionRequest> decodeTransactionRequest(byte[] data);

    /**
     * Check if the request is transaction
     *
     * @param request
     * @return true if transaction
     */
    public boolean isTransaction(Request request);

    /**
     * Decode an encoded block header binary data.
     *
     * @param data the encoded block header
     * @return BlockHeader
     */
    public BlockHeader decodeBlockHeader(byte[] data);

    /**
     * Call the interface of contract or chaincode
     *
     * @param request the transaction request
     * @param connection the connection of a chain
     * @return the transaction response
     */
    @Deprecated
    public TransactionResponse call(
            TransactionContext<TransactionRequest> request, Connection connection)
            throws TransactionException;

    /**
     * Async Call the interface of contract or chaincode Just fake async for compatibility, you need
     * to override this function
     *
     * @param request the transaction request
     * @param connection the connection of a chain
     * @param callback the callback class for async call
     * @return the transaction response
     */
    void asyncCall(
            TransactionContext<TransactionRequest> request,
            Connection connection,
            Driver.Callback callback);

    void asyncCallByProxy(
            TransactionContext<TransactionRequest> request,
            Connection connection,
            Driver.Callback callback);

    /**
     * Send transaction to the interface of contract or chaincode
     *
     * @param request the transaction request
     * @return the transaction response
     */
    @Deprecated
    public TransactionResponse sendTransaction(
            TransactionContext<TransactionRequest> request, Connection connection)
            throws TransactionException;

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
            TransactionContext<TransactionRequest> request,
            Connection connection,
            Driver.Callback callback);

    void asyncSendTransactionByProxy(
            TransactionContext<TransactionRequest> request,
            Connection connection,
            Driver.Callback callback);

    /**
     * Get block number
     *
     * @return block number
     */
    public interface GetBlockNumberCallback {
        public void onResponse(Exception e, long blockNumber);
    }

    public void asyncGetBlockNumber(Connection connection, GetBlockNumberCallback callback);

    /**
     * Get block header
     *
     * @param blockNumber
     * @return BlockHeader
     */
    public interface GetBlockHeaderCallback {
        public void onResponse(Exception e, BlockHeader blockHeader);
    }

    public void asyncGetBlockHeader(
            long blockNumber, Connection connection, GetBlockHeaderCallback callback);

    /**
     * Get verified transaction info of the Chain
     *
     * @param transactionHash
     * @param blockNumber
     * @param blockHeaderManager
     * @param connection
     * @return null if the transaction has not been verified
     */
    public VerifiedTransaction getVerifiedTransaction(
            String transactionHash,
            long blockNumber,
            BlockHeaderManager blockHeaderManager,
            Connection connection);

    interface CustomCommandCallback {
        void onResponse(Exception error, Object response);
    }

    /**
     * Custom command
     *
     * @param path
     * @param args
     * @param account
     * @param blockHeaderManager
     * @param connection
     * @param callback
     */
    public void asyncCustomCommand(
            String command,
            Path path,
            Object[] args,
            Account account,
            BlockHeaderManager blockHeaderManager,
            Connection connection,
            CustomCommandCallback callback
            );
}
