package com.webank.wecross.network.rpc.handler;

import com.webank.wecross.account.UserContext;
import com.webank.wecross.common.NetworkQueryStatus;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.host.WeCrossHost;
import com.webank.wecross.restserver.RestResponse;
import com.webank.wecross.restserver.response.CompleteTransactionResponse;
import com.webank.wecross.restserver.response.TransactionListResponse;
import com.webank.wecross.stub.*;
import com.webank.wecross.zone.Chain;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** GET /transaction/method */
public class TransactionURIHandler implements URIHandler {

    private static final Logger logger = LoggerFactory.getLogger(TransactionURIHandler.class);

    private WeCrossHost host;

    public TransactionURIHandler(WeCrossHost host) {
        this.host = host;
    }

    public WeCrossHost getHost() {
        return host;
    }

    public void setHost(WeCrossHost host) {
        this.host = host;
    }

    @Override
    public void handle(
            UserContext userContext,
            String uri,
            String httpMethod,
            String content,
            Callback callback) {
        RestResponse<Object> restResponse = new RestResponse<>();
        try {

            if (logger.isDebugEnabled()) {
                logger.debug("Uri: {}", uri);
            }

            /* uri: /trans/method?path=payment.bcos&xxx=xxx */
            URI thisUri = URI.create("http://www.wecross.com" + uri);
            String path = thisUri.getPath();
            String method = path.substring(1).split("\\.")[1];
            switch (method.toLowerCase()) {
                case "listTransactions":
                    {
                        try {
                            String[] querys = thisUri.getQuery().split("&");
                            String chainPath = querys[0].substring(5);
                            long blockNumber = Long.parseLong(querys[1].substring(12));
                            int offset = Integer.parseInt(querys[2].substring(7));
                            int size = Integer.parseInt(querys[3].substring(5));

                            if (logger.isDebugEnabled()) {
                                logger.debug(
                                        "chain: {}, blockNumber: {}, offset: {}, size: {}",
                                        chainPath,
                                        blockNumber,
                                        offset,
                                        size);
                            }

                            if (offset < 0 || size <= 0) {
                                restResponse.setErrorCode(NetworkQueryStatus.URI_QUERY_ERROR);
                                restResponse.setMessage("Wrong offset or size");
                                callback.onResponse(restResponse);
                                return;
                            }

                            Chain chain = host.getZoneManager().getChain(Path.decode(chainPath));
                            Driver driver = chain.getDriver();

                            driver.asyncGetBlockNumber(
                                    chain.chooseConnection(),
                                    (getBlockNumberException, currentBlockNumber) -> {
                                        try {
                                            if (Objects.nonNull(getBlockNumberException)) {
                                                logger.warn(
                                                        "Failed to get block number: ",
                                                        getBlockNumberException);
                                                throw new WeCrossException(
                                                        WeCrossException.ErrorCode
                                                                .GET_BLOCK_NUMBER_ERROR,
                                                        getBlockNumberException.getMessage());
                                            }

                                            // update real block number for query
                                            long newBlockNumber = blockNumber;
                                            if (blockNumber < 0
                                                    || blockNumber > currentBlockNumber) {
                                                newBlockNumber = currentBlockNumber;
                                            }

                                            TransactionListResponse response =
                                                    new TransactionListResponse();
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
                                                            content);
                                                }

                                                CompletableFuture<Block> future =
                                                        new CompletableFuture<>();
                                                driver.asyncGetBlock(
                                                        newBlockNumber,
                                                        false,
                                                        chain.chooseConnection(),
                                                        (getBlockException, block) -> {
                                                            if (Objects.nonNull(
                                                                    getBlockException)) {
                                                                logger.warn(
                                                                        "Failed to get block: ",
                                                                        getBlockException);
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
                                                    block =
                                                            future.get(
                                                                    10000, TimeUnit.MILLISECONDS);
                                                } catch (Exception e) {
                                                    future.cancel(true);
                                                    logger.warn("Failed to get block: ", e);
                                                    throw new WeCrossException(
                                                            WeCrossException.ErrorCode
                                                                    .GET_BLOCK_ERROR,
                                                            e.getMessage());
                                                }

                                                if (Objects.isNull(block)) {
                                                    throw new WeCrossException(
                                                            WeCrossException.ErrorCode
                                                                    .GET_BLOCK_ERROR,
                                                            "Failed to get block");
                                                }

                                                List<String> transactionsHashes =
                                                        block.getTransactionsHashes();
                                                int index = nextOffset;
                                                if (index >= transactionsHashes.size()) {
                                                    restResponse.setErrorCode(
                                                            NetworkQueryStatus.URI_QUERY_ERROR);
                                                    restResponse.setMessage("Wrong offset");
                                                    callback.onResponse(restResponse);
                                                    return;
                                                }

                                                for (;
                                                        index < transactionsHashes.size()
                                                                && count < size;
                                                        index++) {
                                                    TransactionListResponse.Transaction
                                                            transaction =
                                                                    new TransactionListResponse
                                                                            .Transaction();
                                                    transaction.setBlockNumber(newBlockNumber);
                                                    transaction.setTxHash(
                                                            transactionsHashes.get(index));
                                                    transactions[count++] = transaction;
                                                }

                                                // update offset
                                                long nextBlockNumber =
                                                        index == transactionsHashes.size()
                                                                ? (newBlockNumber - 1)
                                                                : newBlockNumber;
                                                nextOffset =
                                                        nextBlockNumber == newBlockNumber
                                                                ? index
                                                                : 0;

                                                // done
                                                if (count == size) {
                                                    response.setNextBlockNumber(nextBlockNumber);
                                                    response.setNextOffset(index);
                                                    response.setTransactions(transactions);
                                                    restResponse.setData(response);
                                                    callback.onResponse(restResponse);
                                                    return;
                                                }

                                                // next block
                                                newBlockNumber = nextBlockNumber;

                                                // there is no block anymore
                                                if (newBlockNumber < 0) {
                                                    response.setNextBlockNumber(-1);
                                                    response.setNextOffset(0);
                                                    restResponse.setData(response);
                                                    callback.onResponse(restResponse);
                                                    return;
                                                }
                                            }
                                        } catch (WeCrossException e) {
                                            restResponse.setErrorCode(
                                                    NetworkQueryStatus.TRANSACTION_ERROR
                                                            + e.getErrorCode());
                                            restResponse.setMessage(e.getMessage());
                                            callback.onResponse(restResponse);
                                        }
                                    });
                        } catch (Exception e) {
                            logger.warn("Failed to get transaction list: ", e);
                            restResponse.setErrorCode(WeCrossException.ErrorCode.INTERNAL_ERROR);
                            restResponse.setMessage(e.getMessage());
                            callback.onResponse(restResponse);
                        }
                        return;
                    }
                case "getTransaction":
                    {
                        String[] querys = thisUri.getQuery().split("&");
                        String chainPath = querys[0].substring(5);
                        String txHash = querys[1].substring(7);

                        Chain chain = host.getZoneManager().getChain(Path.decode(chainPath));
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
                                        restResponse.setErrorCode(
                                                WeCrossException.ErrorCode.GET_TRANSACTION_ERROR);
                                        restResponse.setMessage(e.getMessage());
                                        callback.onResponse(restResponse);
                                    } else {
                                        CompleteTransactionResponse completeTransactionResponse =
                                                new CompleteTransactionResponse();
                                        completeTransactionResponse.setTxBytes(
                                                transaction.getTxBytes());
                                        completeTransactionResponse.setReceiptBytes(
                                                transaction.getReceiptBytes());
                                        completeTransactionResponse.setBlockNumber(
                                                transaction.getBlockNumber());
                                        completeTransactionResponse.setTxHash(txHash);

                                        if (transaction.isTransactionByProxy()) {
                                            completeTransactionResponse.setByProxy(true);
                                            completeTransactionResponse.setPath(
                                                    chainPath + "." + transaction.getResource());
                                            completeTransactionResponse.setUsername(
                                                    host.getAccountManager()
                                                            .getUniversalAccountByIdentity(
                                                                    transaction
                                                                            .getAccountIdentity())
                                                            .getName());
                                            completeTransactionResponse.setMethod(
                                                    transaction
                                                            .getTransactionRequest()
                                                            .getMethod());
                                            completeTransactionResponse.setArgs(
                                                    transaction.getTransactionRequest().getArgs());
                                            completeTransactionResponse.setResult(
                                                    transaction
                                                            .getTransactionResponse()
                                                            .getResult());
                                            completeTransactionResponse.setXaTransactionID(
                                                    transaction.getXaTransactionID());
                                            completeTransactionResponse.setXaTransactionSeq(
                                                    transaction.getXaTransactionSeq());
                                        }
                                        restResponse.setData(completeTransactionResponse);
                                        callback.onResponse(restResponse);
                                    }
                                });
                        return;
                    }
                default:
                    {
                        logger.warn("Unsupported method: {}", method);
                        restResponse.setErrorCode(NetworkQueryStatus.URI_PATH_ERROR);
                        restResponse.setMessage("Unsupported method: " + method);
                        break;
                    }
            }
        } catch (Exception e) {
            logger.warn("Process uri error:", e);
            restResponse.setErrorCode(NetworkQueryStatus.INTERNAL_ERROR);
            restResponse.setMessage(e.getLocalizedMessage());
        }
        callback.onResponse(restResponse);
    }
}
