package com.webank.wecross.routine.htlc;

import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.host.WeCrossHost;
import com.webank.wecross.peer.Peer;
import com.webank.wecross.resource.EventCallback;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.resource.ResourceBlockHeaderManager;
import com.webank.wecross.routine.RoutineDefault;
import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.Request;
import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stub.Response;
import com.webank.wecross.stub.TransactionContext;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.TransactionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTLCResource extends Resource {

    private Logger logger = LoggerFactory.getLogger(HTLCResource.class);

    private WeCrossHost weCrossHost;
    private boolean isFresh = false;
    private Resource freshSelfResource;
    private Resource freshCounterpartyResource;
    private Account account;
    private Path selfPath;
    private Path counterpartyPath;
    private String counterpartyAddress;

    public HTLCResource() {}

    public HTLCResource(
            boolean isFresh,
            Resource freshSelfResource,
            Resource freshCounterpartyResource,
            String counterpartyAddress) {
        this.isFresh = isFresh;
        this.freshSelfResource = freshSelfResource;
        this.freshCounterpartyResource = freshCounterpartyResource;
        this.counterpartyAddress = counterpartyAddress;
    }

    public HTLCResource(WeCrossHost weCrossHost, Path selfPath, Path counterpartyPath) {
        this.weCrossHost = weCrossHost;
        this.selfPath = selfPath;
        this.counterpartyPath = counterpartyPath;
    }

    @Override
    public TransactionResponse call(TransactionContext<TransactionRequest> request) {
        return getSelfResource().call(request);
    }

    @Override
    public TransactionResponse sendTransaction(TransactionContext<TransactionRequest> request) {
        try {
            handleSendTransactionRequest(request.getData());
        } catch (WeCrossException e) {
            logger.error(
                    "HTLC sendTransaction, errorCode: {}, errorMsg: {}",
                    e.getErrorCode(),
                    e.getMessage());
            TransactionResponse transactionResponse = new TransactionResponse();
            transactionResponse.setErrorCode(e.getErrorCode());
            transactionResponse.setErrorMessage(e.getMessage());
            return transactionResponse;
        }

        return getSelfResource().sendTransaction(request);
    }

    public void handleSendTransactionRequest(TransactionRequest request) throws WeCrossException {
        if (request.getMethod().equals("unlock")) {
            verifyLock(request);
        }
    }

    public void verifyLock(TransactionRequest request) throws WeCrossException {
        String[] args = request.getArgs();
        if (args == null || args.length != 4) {
            throw new WeCrossException(
                    HTLCErrorCode.ASSET_HTLC_REQUEST_ERROR, "info of lock transaction not found");
        }

        // args: h, s, txHash, blockNumber
        String h = args[0];
        String txhash = args[2];
        long bolckNumber = Long.parseLong(args[3]);
        WeCrossHTLC weCrossHTLC = new WeCrossHTLC();

        VerifyData verifyData =
                new VerifyData(
                        bolckNumber,
                        txhash,
                        counterpartyAddress,
                        "lock",
                        new String[] {h},
                        new String[] {RoutineDefault.SUCCESS_FLAG});

        if (!weCrossHTLC.verify(getCounterpartyResource(), verifyData)) {
            throw new WeCrossException(
                    HTLCErrorCode.ASSET_HTLC_VERIFY_ERROR, "failed to verify lock");
        }
    }

    @Override
    public Response onRemoteTransaction(Request request) {
        //        Driver driver = getDriver();
        //        if (driver.isTransaction(request)) {
        //            TransactionContext<TransactionRequest> context =
        //                    driver.decodeTransactionRequest(request.getData());
        //            TransactionRequest transactionRequest = context.getData();
        //            String method = transactionRequest.getMethod();
        //            if (method.equalsIgnoreCase("getSecret") || method.equalsIgnoreCase("init")) {
        //                String errorMsg = "HTLCResource doesn't allow peers to call
        // \"getSecret\"";
        //                logger.info(errorMsg);
        //                Response response = new Response();
        //                response.setErrorCode(HTLCErrorCode.ASSET_HTLC_NO_PERMISSION);
        //                response.setErrorMessage(errorMsg);
        //                return response;
        //            } else if (transactionRequest.getMethod().equalsIgnoreCase("unlock")) {
        //                try {
        //                    verifyLock(transactionRequest);
        //                } catch (WeCrossException e) {
        //                    String errorMsg =
        //                            "HTLC unlock failed, errorCode: "
        //                                    + e.getErrorCode()
        //                                    + " errorMsg: "
        //                                    + e.getMessage();
        //                    logger.info(errorMsg);
        //                    Response response = new Response();
        //                    response.setErrorCode(HTLCErrorCode.ASSET_HTLC_VERIFY_ERROR);
        //                    response.setErrorMessage(errorMsg);
        //                    return response;
        //                }
        //            }
        //        }
        return getSelfResource().onRemoteTransaction(request);
    }

    public boolean isFresh() {
        return isFresh;
    }

    public void setFresh(boolean fresh) {
        isFresh = fresh;
    }

    public Resource getSelfResource() {
        if (isFresh) {
            return freshSelfResource;
        } else {
            return weCrossHost.getZoneManager().getResource(selfPath);
        }
    }

    public Resource getCounterpartyResource() {
        if (isFresh) {
            return freshCounterpartyResource;
        } else {
            return weCrossHost.getZoneManager().getResource(counterpartyPath);
        }
    }

    public WeCrossHost getWeCrossHost() {
        return weCrossHost;
    }

    public void setWeCrossHost(WeCrossHost weCrossHost) {
        this.weCrossHost = weCrossHost;
    }

    public Path getSelfPath() {
        return selfPath;
    }

    public void setSelfPath(Path selfPath) {
        this.selfPath = selfPath;
    }

    public Path getCounterpartyPath() {
        return counterpartyPath;
    }

    public void setCounterpartyPath(Path counterpartyPath) {
        this.counterpartyPath = counterpartyPath;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getCounterpartyAddress() {
        return counterpartyAddress;
    }

    public void setCounterpartyAddress(String counterpartyAddress) {
        this.counterpartyAddress = counterpartyAddress;
    }

    @Override
    public void addConnection(Peer peer, Connection connection) {
        getSelfResource().addConnection(peer, connection);
    }

    @Override
    public void removeConnection(Peer peer) {
        getSelfResource().removeConnection(peer);
    }

    @Override
    public Connection chooseConnection() {
        return getSelfResource().chooseConnection();
    }

    @Override
    public boolean isConnectionEmpty() {
        return getSelfResource().isConnectionEmpty();
    }

    @Override
    public void registerEventHandler(EventCallback callback) {
        getSelfResource().registerEventHandler(callback);
    }

    @Override
    public String getType() {
        return getSelfResource().getType();
    }

    @Override
    public void setType(String type) {
        getSelfResource().setType(type);
    }

    @Override
    public String getChecksum() {
        return getSelfResource().getChecksum();
    }

    @Override
    public Driver getDriver() {
        return getSelfResource().getDriver();
    }

    @Override
    public void setDriver(Driver driver) {
        getSelfResource().setDriver(driver);
    }

    @Override
    public ResourceInfo getResourceInfo() {
        return getSelfResource().getResourceInfo();
    }

    @Override
    public void setResourceInfo(ResourceInfo resourceInfo) {
        getSelfResource().setResourceInfo(resourceInfo);
    }

    @Override
    public ResourceBlockHeaderManager getResourceBlockHeaderManager() {
        return getSelfResource().getResourceBlockHeaderManager();
    }

    @Override
    public void setResourceBlockHeaderManager(
            ResourceBlockHeaderManager resourceBlockHeaderManager) {
        getSelfResource().setResourceBlockHeaderManager(resourceBlockHeaderManager);
    }

    @Override
    public boolean isHasLocalConnection() {
        return getSelfResource().isHasLocalConnection();
    }
}
