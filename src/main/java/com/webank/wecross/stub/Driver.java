package com.webank.wecross.stub;

public interface Driver {
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
     * @return the transaction response
     */
    public TransactionResponse call(
            TransactionContext<TransactionRequest> request, Connection connection);

    /**
     * Send transaction to the interface of contract or chaincode
     *
     * @param request the transaction request
     * @return the transaction response
     */
    public TransactionResponse sendTransaction(
            TransactionContext<TransactionRequest> request, Connection connection);

    /**
     * Get block number
     *
     * @return block number
     */
    public long getBlockNumber(Connection connection);

    /**
     * Get block header
     *
     * @param number
     * @return BlockHeader
     */
    public byte[] getBlockHeader(long number, Connection connection);

    /**
     * Get verified transaction info of the Chain
     *
     * @param transactionHash
     * @param blockHeaderManager
     * @param connection
     * @return null if the transaction has not been verified
     */
    public VerifiedTransaction getVerifiedTransaction(
            String transactionHash, BlockHeaderManager blockHeaderManager, Connection connection);
}
