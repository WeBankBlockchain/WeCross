package com.webank.wecross.restserver.fetcher;

import com.webank.wecross.account.AccountManager;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.restserver.response.CompleteTransactionResponse;
import com.webank.wecross.restserver.response.TransactionListResponse;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Path;
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
            Path chainPath, String txHash, FetchTransactionCallback callback) {
        Chain chain = zoneManager.getChain(chainPath);
        Driver driver = chain.getDriver();

        driver.asyncGetTransaction(
                txHash,
                0,
                chain.getBlockManager(),
                false,
                chain.chooseConnection(),
                (e, transaction) -> {
                    if (Objects.nonNull(e)) {
                        logger.warn("Failed to get transaction: ", e);
                        callback.onResponse(
                                new WeCrossException(
                                        WeCrossException.ErrorCode.GET_TRANSACTION_ERROR,
                                        e.getMessage()),
                                null);
                        return;
                    }

                    CompleteTransactionResponse completeTransactionResponse =
                            new CompleteTransactionResponse();
                    completeTransactionResponse.setTxBytes(transaction.getTxBytes());
                    completeTransactionResponse.setReceiptBytes(transaction.getReceiptBytes());
                    completeTransactionResponse.setBlockNumber(transaction.getBlockNumber());
                    completeTransactionResponse.setTxHash(txHash);

                    if (transaction.isTransactionByProxy()) {
                        completeTransactionResponse.setByProxy(true);
                        completeTransactionResponse.setPath(
                                chainPath + "." + transaction.getResource());
                        completeTransactionResponse.setUsername(
                                accountManager
                                        .getUniversalAccountByIdentity(
                                                transaction.getAccountIdentity())
                                        .getName());
                        completeTransactionResponse.setMethod(
                                transaction.getTransactionRequest().getMethod());
                        completeTransactionResponse.setArgs(
                                transaction.getTransactionRequest().getArgs());
                        completeTransactionResponse.setResult(
                                transaction.getTransactionResponse().getResult());
                        completeTransactionResponse.setXaTransactionID(
                                transaction.getXaTransactionID());
                        completeTransactionResponse.setXaTransactionSeq(
                                transaction.getXaTransactionSeq());
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
                        mainCallback.onResponse(null, response);
                        return;
                    }

                    if (Objects.isNull(block)) {
                        logger.warn("Current block is null, response: {}", response);
                        mainCallback.onResponse(null, response);
                        return;
                    }

                    if (logger.isDebugEnabled()) {
                        logger.debug(
                                "Fetch block, blockNumber: {}, block: {}, response: {}",
                                blockNumber,
                                block,
                                response);
                    }

                    int offset = response.getNextOffset();
                    if (offset >= block.transactionsHashes.size()) {
                        logger.warn(
                                "Wrong offset, total txHash: {}, offset: {}, response: {}",
                                block.transactionsHashes.size(),
                                offset,
                                response);
                        mainCallback.onResponse(null, response);
                        return;
                    }

                    int index;
                    int count = size;
                    for (index = offset;
                            index < block.transactionsHashes.size() && count > 0;
                            index++, count--) {
                        TransactionListResponse.Transaction transaction =
                                new TransactionListResponse.Transaction();
                        transaction.setBlockNumber(blockNumber);
                        transaction.setTxHash(block.transactionsHashes.get(index));
                        response.addTransaction(transaction);
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
