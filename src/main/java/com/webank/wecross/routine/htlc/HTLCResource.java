package com.webank.wecross.routine.htlc;

import com.webank.wecross.account.UniversalAccount;
import com.webank.wecross.common.NetworkQueryStatus;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.exception.WeCrossException.ErrorCode;
import com.webank.wecross.peer.Peer;
import com.webank.wecross.resource.EventCallback;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.routine.RoutineDefault;
import com.webank.wecross.routine.TransactionValidator;
import com.webank.wecross.stub.*;
import com.webank.wecross.zone.ZoneManager;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTLCResource extends Resource {

    private Logger logger = LoggerFactory.getLogger(HTLCResource.class);

    private ZoneManager zoneManager;
    private UniversalAccount adminUa;
    private Path selfPath;
    private Path counterpartyPath;

    private static final Set<String> P2P_ACCESS_WHITE_LIST =
            new HashSet<String>() {
                {
                    // TODO: optimize this, this method is not belongs to HTLC
                    // */
                    // for performance test, remove before releasing
                    add("newProposal");
                    add("setNewProposalTxInfo");
                    add("setSecret");
                    // */

                    add("selectByName");
                    add("readlink");
                    add("lock");
                    add("unlock");
                    add("balanceOf");
                    add("getNewProposalTxInfo");
                    add("getCounterpartyHtlcAddress");
                }
            };

    public HTLCResource() {}

    public HTLCResource(ZoneManager zoneManager, Path selfPath, Path counterpartyPath) {
        this.zoneManager = zoneManager;
        this.selfPath = selfPath;
        this.counterpartyPath = counterpartyPath;
    }

    @Override
    public void asyncCall(
            TransactionRequest request, UniversalAccount ua, Resource.Callback callback) {
        getSelfResource().asyncCall(request, ua, callback);
    }

    @Override
    public void asyncSendTransaction(
            TransactionRequest request, UniversalAccount ua, Resource.Callback callback) {
        if (getSelfResource().hasLocalConnection()) {
            TransactionRequest transactionRequest = request;
            if (RoutineDefault.UNLOCK_METHOD.equals(transactionRequest.getMethod())) {
                handleUnlockRequest(
                        request,
                        exception -> {
                            if (exception != null) {
                                TransactionResponse transactionResponse = new TransactionResponse();
                                transactionResponse.setErrorCode(exception.getErrorCode());
                                transactionResponse.setMessage(exception.getMessage());
                                callback.onTransactionResponse(
                                        new TransactionException(0, null), transactionResponse);
                            } else {
                                getSelfResource().asyncSendTransaction(request, ua, callback);
                            }
                        });
            } else {
                getSelfResource().asyncSendTransaction(request, ua, callback);
            }
        } else {
            getSelfResource().asyncSendTransaction(request, ua, callback);
        }
    }

    public interface HandleUnlockRequestCallback {
        void onReturn(WeCrossException exception);
    }

    public void handleUnlockRequest(
            TransactionRequest request, HandleUnlockRequestCallback callback) {
        String[] args = request.getArgs();
        if (args == null || args.length != 2) {
            callback.onReturn(new WeCrossException(HTLCErrorCode.UNLOCK_ERROR, "incomplete args"));
            return;
        }

        String hash = args[0];
        HTLC htlc = new HTLCImpl();
        htlc.getProposalInfo(
                getSelfResource(),
                adminUa,
                hash,
                (exception, result) -> {
                    if (exception != null) {
                        callback.onReturn(exception);
                        return;
                    }

                    if (result == null || RoutineDefault.NULL_FLAG.equals(result)) {
                        callback.onReturn(
                                new WeCrossException(
                                        HTLCErrorCode.UNLOCK_ERROR, "proposal not found"));
                        return;
                    }

                    HTLCProposal proposal;
                    try {
                        // decode proposal
                        proposal = htlc.decodeProposal(result.split(RoutineDefault.SPLIT_REGEX));
                    } catch (WeCrossException e) {
                        callback.onReturn(
                                new WeCrossException(
                                        HTLCErrorCode.UNLOCK_ERROR, "failed to decode proposal"));
                        return;
                    }

                    if (!proposal.isInitiator() && !proposal.isCounterpartyUnlocked()) {
                        // participant will unlock initiator firstly
                        unlockCounterparty(
                                request,
                                exception1 -> {
                                    if (exception1 != null) {
                                        callback.onReturn(exception1);
                                        return;
                                    }

                                    if (logger.isDebugEnabled()) {
                                        logger.debug(
                                                "Participant unlocks initiator successfully, request: {}",
                                                request);
                                    }

                                    htlc.setCounterpartyUnlockState(
                                            getSelfResource(),
                                            adminUa,
                                            hash,
                                            (exception2, result2) -> callback.onReturn(exception2));
                                });
                    } else {
                        callback.onReturn(null);
                    }
                });
    }

    public interface UnlockCounterpartyCallback {
        void onReturn(WeCrossException exception);
    }

    public void unlockCounterparty(
            TransactionRequest request, UnlockCounterpartyCallback callback) {
        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Participant receives a unlock request, and unlocks initiator firstly, request: {}",
                    request);
        }
        getCounterpartyResource()
                .asyncSendTransaction(
                        request,
                        adminUa,
                        (transactionException, transactionResponse) -> {
                            if (transactionException != null && !transactionException.isSuccess()) {
                                callback.onReturn(
                                        new WeCrossException(
                                                HTLCErrorCode.UNLOCK_ERROR,
                                                "participant failed to unlock initiator"));
                                logger.error(
                                        "UNLOCK_INITIATOR_ERROR: {}, {}",
                                        transactionException.getErrorCode(),
                                        transactionException.getMessage());
                                return;
                            }

                            if (transactionResponse.getErrorCode() != 0) {
                                callback.onReturn(
                                        new WeCrossException(
                                                HTLCErrorCode.UNLOCK_ERROR,
                                                "participant failed to unlock initiator"));
                                logger.error(
                                        "UNLOCK_INITIATOR_ERROR: {}, {}",
                                        transactionResponse.getErrorCode(),
                                        transactionResponse.getMessage());
                                return;
                            }

                            verifyUnlock(
                                    transactionResponse,
                                    request.getArgs(),
                                    (exception, result) -> {
                                        if (exception != null || !result) {
                                            callback.onReturn(
                                                    new WeCrossException(
                                                            ErrorCode.HTLC_ERROR,
                                                            "participant failed to verify unlock"));
                                        } else {
                                            callback.onReturn(null);
                                        }
                                    });
                        });
    }

    public interface VerifyUnlockCallback {
        void onReturn(WeCrossException exception, boolean result);
    }

    public void verifyUnlock(
            TransactionResponse transactionResponse, String[] args, VerifyUnlockCallback callback) {
        HTLC htlc = new HTLCImpl();

        TransactionValidator transactionValidator =
                new TransactionValidator(
                        transactionResponse.getBlockNumber(),
                        transactionResponse.getHash(),
                        RoutineDefault.UNLOCK_METHOD,
                        args,
                        new String[] {RoutineDefault.SUCCESS_FLAG});
        transactionValidator.setPath(getCounterpartyPath());
        htlc.verifyHtlcTransaction(
                getCounterpartyResource(),
                transactionValidator,
                (exception, result) -> {
                    if (exception != null) {
                        logger.error("failed to verify unlock,", exception);
                        callback.onReturn(
                                new WeCrossException(
                                        ErrorCode.HTLC_ERROR, exception.getInternalMessage()),
                                false);
                    } else {
                        callback.onReturn(null, result);
                    }
                });
    }

    @Override
    public void onRemoteTransaction(Request request, Connection.Callback callback) {
        Response response = new Response();
        Driver driver = getDriver();

        ImmutablePair<Boolean, TransactionRequest> booleanTransactionRequestPair =
                driver.decodeTransactionRequest(request);
        if (booleanTransactionRequestPair.getKey()) {
            if (logger.isDebugEnabled()) {
                logger.debug("onRemoteTransaction, request: {}", request);
            }

            TransactionRequest transactionRequest = booleanTransactionRequestPair.getValue();
            if (transactionRequest == null) {
                response.setErrorCode(
                        NetworkQueryStatus.HTLC_ERROR
                                + HTLCErrorCode.DECODE_TRANSACTION_REQUEST_ERROR);
                response.setErrorMessage("decode transaction request failed");
                if (logger.isDebugEnabled()) {
                    logger.debug("onRemoteTransaction, response: {}", response);
                }

                callback.onResponse(response);
                return;
            }

            if (logger.isDebugEnabled()) {
                logger.debug("onRemoteTransaction, transactionRequest: {}", transactionRequest);
            }

            String method = transactionRequest.getMethod();
            if (RoutineDefault.UNLOCK_METHOD.equals(method)) {
                handleUnlockRequest(
                        transactionRequest,
                        exception -> {
                            if (exception != null) {
                                Response newResponse = new Response();
                                newResponse.setErrorCode(
                                        NetworkQueryStatus.HTLC_ERROR + HTLCErrorCode.UNLOCK_ERROR);
                                newResponse.setErrorMessage(exception.getMessage());
                                if (logger.isDebugEnabled()) {
                                    logger.debug("onRemoteTransaction, response: {}", newResponse);
                                }
                                callback.onResponse(newResponse);
                            } else {
                                getSelfResource()
                                        .onRemoteTransaction(
                                                request,
                                                response1 -> {
                                                    if (logger.isDebugEnabled()) {
                                                        logger.debug(
                                                                "onRemoteTransaction, response: {}",
                                                                response1);
                                                    }
                                                    callback.onResponse(response1);
                                                });
                            }
                        });
                return;
            }

            if (!P2P_ACCESS_WHITE_LIST.contains(method)) {
                response = new Response();
                response.setErrorCode(NetworkQueryStatus.HTLC_ERROR + HTLCErrorCode.NO_PERMISSION);
                response.setErrorMessage(
                        "HTLCResource doesn't allow peers to call,request: " + transactionRequest);
                if (logger.isDebugEnabled()) {
                    logger.debug("onRemoteTransaction, response: {}", response);
                }

                callback.onResponse(response);
                return;
            }
        }

        getSelfResource()
                .onRemoteTransaction(
                        request,
                        response2 -> {
                            if (logger.isDebugEnabled()) {
                                logger.debug("onRemoteTransaction, response: {}", response2);
                            }

                            callback.onResponse(response2);
                        });
    }

    public Resource getSelfResource() {
        return zoneManager.fetchResource(selfPath);
    }

    public Resource getCounterpartyResource() {
        return zoneManager.fetchResource(counterpartyPath);
    }

    public ZoneManager getZoneManager() {
        return zoneManager;
    }

    public void setZoneManager(ZoneManager zoneManager) {
        this.zoneManager = zoneManager;
    }

    public UniversalAccount getAdminUa() {
        return adminUa;
    }

    public void setAdminUa(UniversalAccount adminUa) {
        this.adminUa = adminUa;
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

    @Override
    public Path getPath() {
        return selfPath;
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
    public String getStubType() {
        return getSelfResource().getStubType();
    }

    @Override
    public void setStubType(String type) {
        getSelfResource().setStubType(type);
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
    public BlockManager getBlockManager() {
        return getSelfResource().getBlockManager();
    }

    @Override
    public void setBlockManager(BlockManager blockManager) {
        getSelfResource().setBlockManager(blockManager);
    }

    @Override
    public boolean hasLocalConnection() {
        return getSelfResource().hasLocalConnection();
    }

    @Override
    public String toString() {
        return "HTLCResource{"
                + "zoneManager="
                + zoneManager
                + ", adminUa="
                + adminUa
                + ", selfPath="
                + selfPath
                + ", counterpartyPath="
                + counterpartyPath
                + '}';
    }
}
