package com.webank.wecross.restserver.fetcher;

import com.webank.wecross.account.AccountManager;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.restserver.response.CompleteTransactionResponse;
import com.webank.wecross.restserver.response.TransactionListResponse;
import com.webank.wecross.stub.Block;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Path;
import com.webank.wecross.zone.Chain;
import com.webank.wecross.zone.ZoneManager;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
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

    public interface FetchTransactionListCallback {
        void onResponse(WeCrossException e, TransactionListResponse response);
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
                    try {
                        if (Objects.nonNull(getBlockNumberException)) {
                            logger.warn("Failed to get block number: ", getBlockNumberException);
                            throw new WeCrossException(
                                    WeCrossException.ErrorCode.GET_BLOCK_NUMBER_ERROR,
                                    getBlockNumberException.getMessage());
                        }

                        // update real block number for query
                        long newBlockNumber = blockNumber;
                        if (blockNumber < 0 || blockNumber > currentBlockNumber) {
                            newBlockNumber = currentBlockNumber;
                        }

                        TransactionListResponse response = new TransactionListResponse();
                        TransactionListResponse.Transaction[] transactions =
                                new TransactionListResponse.Transaction[size];

                        int count = 0;
                        int nextOffset = offset;
                        while (count < size) {
                            if (logger.isDebugEnabled()) {
                                logger.debug(
                                        "blockNumber: {}, offset: {}, count: {}",
                                        newBlockNumber,
                                        nextOffset,
                                        count);
                            }

                            CompletableFuture<Block> future = new CompletableFuture<>();
                            driver.asyncGetBlock(
                                    newBlockNumber,
                                    false,
                                    chain.chooseConnection(),
                                    (getBlockException, block) -> {
                                        if (Objects.nonNull(getBlockException)) {
                                            logger.warn("Failed to get block: ", getBlockException);
                                            if (!future.isCancelled()) {
                                                future.complete(null);
                                            }
                                        } else {
                                            if (!future.isCancelled()) {
                                                future.complete(block);
                                            }
                                        }
                                    });

                            Block block;
                            try {
                                block = future.get(10000, TimeUnit.MILLISECONDS);
                            } catch (Exception e) {
                                future.cancel(true);
                                logger.warn("Failed to get block: ", e);
                                throw new WeCrossException(
                                        WeCrossException.ErrorCode.GET_BLOCK_ERROR, e.getMessage());
                            }

                            if (Objects.isNull(block)) {
                                throw new WeCrossException(
                                        WeCrossException.ErrorCode.GET_BLOCK_ERROR,
                                        "Failed to get block");
                            }

                            List<String> transactionsHashes = block.getTransactionsHashes();
                            int index = nextOffset;
                            if (index >= transactionsHashes.size()) {
                                throw new WeCrossException(
                                        WeCrossException.ErrorCode.GET_BLOCK_ERROR, "Wrong offset");
                            }

                            for (; index < transactionsHashes.size() && count < size; index++) {
                                TransactionListResponse.Transaction transaction =
                                        new TransactionListResponse.Transaction();
                                transaction.setBlockNumber(newBlockNumber);
                                transaction.setTxHash(transactionsHashes.get(index));
                                transactions[count++] = transaction;
                            }

                            // update offset
                            long nextBlockNumber =
                                    index == transactionsHashes.size()
                                            ? (newBlockNumber - 1)
                                            : newBlockNumber;
                            nextOffset = nextBlockNumber == newBlockNumber ? index : 0;

                            // done
                            if (count == size) {
                                response.setNextBlockNumber(nextBlockNumber);
                                response.setNextOffset(index);
                                response.setTransactions(transactions);
                                callback.onResponse(null, response);
                                return;
                            }

                            // next block
                            newBlockNumber = nextBlockNumber;

                            // there is no block anymore
                            if (newBlockNumber < 0) {
                                response.setNextBlockNumber(-1);
                                response.setNextOffset(0);
                                callback.onResponse(null, response);
                                return;
                            }
                        }
                    } catch (WeCrossException e) {
                        callback.onResponse(e, null);
                    }
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
