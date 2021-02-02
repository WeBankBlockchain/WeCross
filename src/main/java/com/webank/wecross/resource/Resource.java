package com.webank.wecross.resource;

import com.webank.wecross.account.UniversalAccount;
import com.webank.wecross.peer.Peer;
import com.webank.wecross.stub.*;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Resource {
    private Logger logger = LoggerFactory.getLogger(Response.class);
    private String stubType;
    private Driver driver;
    private Map<Peer, Connection> connections = new HashMap<Peer, Connection>();
    private Path path;
    private ResourceInfo resourceInfo;
    private BlockManager blockManager;
    boolean hasLocalConnection = false;
    boolean isTemporary = false;
    private Random random = new SecureRandom();

    public static final String RAW_TRANSACTION = "RAW_TRANSACTION";

    public Map<Peer, Connection> getConnections() {
        return connections;
    }

    public void setConnection(Map<Peer, Connection> connections) {
        this.connections = connections;
    }

    public void addConnection(Peer peer, Connection connection) {
        if (!hasLocalConnection) {
            if (peer == null) {
                connections.clear();
                hasLocalConnection = true;
            }

            connections.put(peer, connection);
        }
    }

    public void removeConnection(Peer peer) {
        if (!hasLocalConnection) {
            connections.remove(peer);
        }
    }

    public boolean isConnectionEmpty() {
        return connections.isEmpty();
    }

    public Connection chooseConnection() {
        if (connections == null || connections.size() == 0) {
            return null;
        } else {
            int index = random.nextInt(connections.size());
            return (Connection) connections.values().toArray()[index];
        }
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    public interface Callback {
        void onTransactionResponse(
                TransactionException transactionException, TransactionResponse transactionResponse);
    }

    public void asyncCall(
            TransactionRequest request, UniversalAccount ua, Resource.Callback callback) {
        try {
            checkAccount(ua);
        } catch (TransactionException e) {
            callback.onTransactionResponse(e, null);
            return;
        }

        Account account = ua.getAccount(stubType);
        TransactionContext context =
                new TransactionContext(account, this.path, this.resourceInfo, this.blockManager);
        boolean isRawTransaction =
                (boolean) request.getOptions().getOrDefault(RAW_TRANSACTION, false);
        if (isRawTransaction) {
            driver.asyncCall(
                    context,
                    request,
                    false,
                    chooseConnection(),
                    (transactionException, transactionResponse) -> {
                        if (logger.isDebugEnabled()) {
                            logger.debug(
                                    "asyncCall response: {}, exception: {}",
                                    transactionResponse,
                                    transactionException);
                        }
                        callback.onTransactionResponse(transactionException, transactionResponse);
                    });
        } else {
            driver.asyncCall(
                    context,
                    request,
                    true,
                    chooseConnection(),
                    (transactionException, transactionResponse) -> {
                        if (logger.isDebugEnabled()) {
                            logger.debug(
                                    "asyncCall response: {}, exception: {}",
                                    transactionResponse,
                                    transactionException);
                        }
                        callback.onTransactionResponse(transactionException, transactionResponse);
                    });
        }
    }

    public void asyncSendTransaction(
            TransactionRequest request, UniversalAccount ua, Resource.Callback callback) {
        try {
            checkAccount(ua);
        } catch (TransactionException e) {
            callback.onTransactionResponse(e, null);
            return;
        }

        Account account = ua.getAccount(stubType);
        TransactionContext context =
                new TransactionContext(account, this.path, this.resourceInfo, this.blockManager);
        boolean isRawTransaction =
                (boolean) request.getOptions().getOrDefault(RAW_TRANSACTION, false);
        if (isRawTransaction) {
            driver.asyncSendTransaction(
                    context,
                    request,
                    false,
                    chooseConnection(),
                    (transactionException, transactionResponse) -> {
                        if (logger.isDebugEnabled()) {
                            logger.debug(
                                    "asyncSendTransaction response: {}, exception: ",
                                    transactionResponse,
                                    transactionException);
                        }
                        callback.onTransactionResponse(transactionException, transactionResponse);
                    });
        } else {
            driver.asyncSendTransaction(
                    context,
                    request,
                    true,
                    chooseConnection(),
                    (transactionException, transactionResponse) -> {
                        if (logger.isDebugEnabled()) {
                            logger.debug(
                                    "asyncSendTransaction response: {}, exception: ",
                                    transactionResponse,
                                    transactionException);
                        }
                        callback.onTransactionResponse(transactionException, transactionResponse);
                    });
        }
    }

    public void onRemoteTransaction(Request request, Connection.Callback callback) {
        request.setResourceInfo(resourceInfo);
        chooseConnection().asyncSend(request, callback);
    }

    public Response onRemoteTransaction(Request request) {
        CompletableFuture<Response> completableFuture = new CompletableFuture<>();

        onRemoteTransaction(request, response -> completableFuture.complete(response));

        try {
            return completableFuture.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            Response response = new Response();
            response.setErrorCode(StubQueryStatus.TIMEOUT);
            response.setErrorMessage("onRemoteTransaction completableFuture exception: " + e);
            logger.error("onRemoteTransaction timeout, resource: {}", getResourceInfo());
            return response;
        }
    }

    private void checkAccount(UniversalAccount ua) throws TransactionException {
        if (Objects.isNull(ua)) {
            throw new TransactionException(
                    TransactionException.ErrorCode.ACCOUNT_ERRPR, "UniversalAccount is null");
        }

        if (Objects.isNull(ua.getAccount(stubType))) {
            throw new TransactionException(
                    TransactionException.ErrorCode.ACCOUNT_ERRPR,
                    "Account with type '" + stubType + "' not found for " + ua.getName());
        }
    }

    public void registerEventHandler(EventCallback callback) {}

    public String getStubType() {
        return stubType;
    }

    public void setStubType(String type) {
        this.stubType = type;
    }

    public String getChecksum() {
        return "";
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public ResourceInfo getResourceInfo() {
        return resourceInfo;
    }

    public void setResourceInfo(ResourceInfo resourceInfo) {
        this.resourceInfo = resourceInfo;
    }

    public BlockManager getBlockManager() {
        return blockManager;
    }

    public void setBlockManager(BlockManager blockManager) {
        this.blockManager = blockManager;
    }

    public boolean hasLocalConnection() {
        return hasLocalConnection;
    }

    public boolean isTemporary() {
        return isTemporary;
    }

    public void setTemporary(boolean isTemporary) {
        this.isTemporary = isTemporary;
        if (isTemporary) {
            resourceInfo.getProperties().put("isTemporary", "true");
        }
    }

    public boolean isOnlyLocal() {
        return hasLocalConnection && connections.size() == 1;
    }
}
