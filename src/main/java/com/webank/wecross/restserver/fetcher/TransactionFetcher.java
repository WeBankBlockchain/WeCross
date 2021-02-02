package com.webank.wecross.restserver.fetcher;

import com.webank.wecross.account.AccountManager;
import com.webank.wecross.account.UniversalAccount;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.restserver.response.CompleteTransactionResponse;
import com.webank.wecross.restserver.response.TransactionListResponse;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.StubConstant;
import com.webank.wecross.zone.Chain;
import com.webank.wecross.zone.ZoneManager;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionFetcher {
    private Logger logger = LoggerFactory.getLogger(TransactionFetcher.class);

    private ZoneManager zoneManager;

    private AccountManager accountManager;

    public TransactionFetcher(ZoneManager zoneManager, AccountManager accountManager) {
        this.zoneManager = zoneManager;
        this.accountManager = accountManager;
    }

    public interface FetchTransactionCallback {
        void onResponse(WeCrossException e, CompleteTransactionResponse response);
    }

    public void asyncFetchTransaction(
            Path chainPath, String txHash, Long blockNumber, FetchTransactionCallback callback) {
        Chain chain = zoneManager.getChain(chainPath);
        Driver driver = chain.getDriver();

        driver.asyncGetTransaction(
                txHash,
                blockNumber,
                chain.getBlockManager(),
                true,
                chain.chooseConnection(),
                (e, transaction) -> {
                    if (Objects.nonNull(e)) {
                        logger.warn(
                                "Failed to get transaction, chain: {}, txHash: {}, e:",
                                chainPath,
                                txHash,
                                e);
                        callback.onResponse(
                                new WeCrossException(
                                        WeCrossException.ErrorCode.GET_TRANSACTION_ERROR,
                                        e.getMessage()),
                                null);
                        return;
                    }

                    if (logger.isDebugEnabled()) {
                        logger.debug(
                                "txHash: {}, blockNumber: {}, transaction: {}",
                                txHash,
                                blockNumber,
                                transaction);
                    }

                    CompleteTransactionResponse completeTransactionResponse =
                            new CompleteTransactionResponse();
                    completeTransactionResponse.setTxBytes(transaction.getTxBytes());
                    completeTransactionResponse.setReceiptBytes(transaction.getReceiptBytes());
                    completeTransactionResponse.setBlockNumber(blockNumber);
                    completeTransactionResponse.setTxHash(txHash);

                    if (transaction.isTransactionByProxy()) {
                        completeTransactionResponse.setByProxy(true);
                        completeTransactionResponse.setPath(
                                chainPath + "." + transaction.getResource());
                        UniversalAccount ua =
                                accountManager.getUniversalAccountByIdentity(
                                        transaction.getAccountIdentity());
                        String username = Objects.nonNull(ua) ? ua.getUsername() : null;
                        completeTransactionResponse.setUsername(username);
                        completeTransactionResponse.setMethod(
                                transaction.getTransactionRequest().getMethod());
                        completeTransactionResponse.setArgs(
                                transaction.getTransactionRequest().getArgs());
                        completeTransactionResponse.setResult(
                                transaction.getTransactionResponse().getResult());
                        String xaTransactionID =
                                (String)
                                        transaction
                                                .getTransactionRequest()
                                                .getOptions()
                                                .get(StubConstant.XA_TRANSACTION_ID);
                        completeTransactionResponse.setXaTransactionID(
                                Objects.isNull(xaTransactionID) ? "0" : xaTransactionID);
                        Long xaTransactionSeq =
                                (Long)
                                        transaction
                                                .getTransactionRequest()
                                                .getOptions()
                                                .get(StubConstant.XA_TRANSACTION_SEQ);
                        completeTransactionResponse.setXaTransactionSeq(
                                Objects.isNull(xaTransactionSeq) ? 0 : xaTransactionSeq);
                        if (transaction.getTransactionResponse().getErrorCode() != 0) {
                            completeTransactionResponse.setErrorCode(
                                    transaction.getTransactionResponse().getErrorCode());
                            completeTransactionResponse.setMessage(
                                    transaction.getTransactionResponse().getMessage());
                        }
                    }
                    callback.onResponse(null, completeTransactionResponse);
                });
    }

    public interface FetchTransactionListCallback {
        void onResponse(WeCrossException e, TransactionListResponse response);
    }

    public void asyncFetchTransactionList(
            Path chainPath,
            long blockNumber,
            int offset,
            int size,
            FetchTransactionListCallback callback) {
        Chain chain = zoneManager.getChain(chainPath);
        Driver driver = chain.getDriver();

        driver.asyncGetBlockNumber(
                chain.chooseConnection(),
                (getBlockNumberException, currentBlockNumber) -> {
                    if (Objects.nonNull(getBlockNumberException)) {
                        logger.warn("Failed to get block number: ", getBlockNumberException);
                        callback.onResponse(
                                new WeCrossException(
                                        WeCrossException.ErrorCode.GET_BLOCK_NUMBER_ERROR,
                                        getBlockNumberException.getMessage()),
                                null);
                        return;
                    }

                    // update real block number for query
                    long newBlockNumber = blockNumber;
                    int newOffset = offset;
                    if (blockNumber < 0 || blockNumber > currentBlockNumber) {
                        newBlockNumber = currentBlockNumber;
                        newOffset = 0;
                    }

                    TransactionListResponse response = new TransactionListResponse();
                    response.setNextBlockNumber(newBlockNumber);
                    response.setNextOffset(newOffset);

                    recursiveFetchTransactionList(chain, driver, size, response, callback);
                });
    }

    private void recursiveFetchTransactionList(
            Chain chain,
            Driver driver,
            int size,
            TransactionListResponse response,
            FetchTransactionListCallback mainCallback) {

        if (logger.isDebugEnabled()) {
            logger.debug("Fetch transaction list, size: {}, response: {}", size, response);
        }

        long blockNumber = response.getNextBlockNumber();
        if (size == 0 || blockNumber == -1) {
            mainCallback.onResponse(null, response);
            return;
        }

        driver.asyncGetBlock(
                blockNumber,
                false,
                chain.chooseConnection(),
                (getBlockException, block) -> {
                    if (Objects.nonNull(getBlockException)) {
                        logger.warn(
                                "Failed to get block, response: {}, error: ",
                                response,
                                getBlockException);
                        mainCallback.onResponse(
                                new WeCrossException(
                                        WeCrossException.ErrorCode.GET_BLOCK_ERROR,
                                        getBlockException.getMessage()),
                                null);
                        return;
                    }

                    if (Objects.isNull(block)) {
                        logger.warn("Current block is null, response: {}", response);
                        mainCallback.onResponse(
                                new WeCrossException(
                                        WeCrossException.ErrorCode.GET_BLOCK_ERROR,
                                        "Block is null"),
                                null);
                        return;
                    }

                    if (logger.isDebugEnabled()) {
                        logger.debug(
                                "Fetch block, blockNumber: {}, block: {}, response: {}",
                                blockNumber,
                                block,
                                response);
                    }

                    if (block.transactionsHashes.isEmpty()) {
                        // blank block
                        response.setNextBlockNumber(blockNumber - 1);
                        response.setNextOffset(0);
                        recursiveFetchTransactionList(chain, driver, size, response, mainCallback);
                        return;
                    }

                    int offset = response.getNextOffset();
                    if (offset >= block.transactionsHashes.size()) {
                        logger.warn(
                                "Wrong offset, total txHash: {}, offset: {}, response: {}",
                                block.transactionsHashes.size(),
                                offset,
                                response);
                        mainCallback.onResponse(
                                new WeCrossException(
                                        WeCrossException.ErrorCode.GET_BLOCK_ERROR, "Wrong offset"),
                                null);
                        return;
                    }

                    int index;
                    int count = size;
                    for (index = offset;
                            index < block.transactionsHashes.size() && count > 0;
                            index++) {
                        // hash is blank
                        if (!"".equals(block.transactionsHashes.get(index).trim())) {
                            TransactionListResponse.Transaction transaction =
                                    new TransactionListResponse.Transaction();
                            transaction.setBlockNumber(blockNumber);
                            transaction.setTxHash(block.transactionsHashes.get(index));
                            response.addTransaction(transaction);
                            count--;
                        }
                    }

                    long nextBlockNumber =
                            index == block.transactionsHashes.size()
                                    ? blockNumber - 1
                                    : blockNumber;
                    int nextOffset = index == block.transactionsHashes.size() ? 0 : index;
                    response.setNextBlockNumber(nextBlockNumber);
                    response.setNextOffset(nextOffset);

                    recursiveFetchTransactionList(chain, driver, count, response, mainCallback);
                });
    }

    public ZoneManager getZoneManager() {
        return zoneManager;
    }

    public void setZoneManager(ZoneManager zoneManager) {
        this.zoneManager = zoneManager;
    }

    public AccountManager getAccountManager() {
        return accountManager;
    }

    public void setAccountManager(AccountManager accountManager) {
        this.accountManager = accountManager;
    }
}
