package com.webank.wecross.resource;

import com.webank.wecross.peer.Peer;
import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.BlockManager;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.Request;
import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stub.Response;
import com.webank.wecross.stub.StubQueryStatus;
import com.webank.wecross.stub.TransactionContext;
import com.webank.wecross.stub.TransactionException;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.TransactionResponse;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
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
        public void onTransactionResponse(
                TransactionException transactionException, TransactionResponse transactionResponse);
    }

    public void asyncCall(TransactionRequest request, Account account, Resource.Callback callback) {
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
                    new Driver.Callback() {
                        @Override
                        public void onTransactionResponse(
                                TransactionException transactionException,
                                TransactionResponse transactionResponse) {
                            if (logger.isDebugEnabled()) {
                                logger.debug(
                                        "asyncCall response: {}, exception: ",
                                        transactionResponse,
                                        transactionException);
                            }
                            callback.onTransactionResponse(
                                    transactionException, transactionResponse);
                        }
                    });
        } else {
            driver.asyncCall(
                    context,
                    request,
                    true,
                    chooseConnection(),
                    new Driver.Callback() {
                        @Override
                        public void onTransactionResponse(
                                TransactionException transactionException,
                                TransactionResponse transactionResponse) {
                            if (logger.isDebugEnabled()) {
                                logger.debug(
                                        "asyncCall response: {}, exception: ",
                                        transactionResponse,
                                        transactionException);
                            }
                            callback.onTransactionResponse(
                                    transactionException, transactionResponse);
                        }
                    });
        }
    }

    public void asyncSendTransaction(
            TransactionRequest request, Account account, Resource.Callback callback) {
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
                    new Driver.Callback() {
                        @Override
                        public void onTransactionResponse(
                                TransactionException transactionException,
                                TransactionResponse transactionResponse) {
                            if (logger.isDebugEnabled()) {
                                logger.debug(
                                        "asyncCall response: {}, exception: ",
                                        transactionResponse,
                                        transactionException);
                            }
                            callback.onTransactionResponse(
                                    transactionException, transactionResponse);
                        }
                    });
        } else {
            driver.asyncSendTransaction(
                    context,
                    request,
                    true,
                    chooseConnection(),
                    new Driver.Callback() {
                        @Override
                        public void onTransactionResponse(
                                TransactionException transactionException,
                                TransactionResponse transactionResponse) {
                            if (logger.isDebugEnabled()) {
                                logger.debug(
                                        "asyncCall response: {}, exception: ",
                                        transactionResponse,
                                        transactionException);
                            }
                            callback.onTransactionResponse(
                                    transactionException, transactionResponse);
                        }
                    });
        }
    }

    public void onRemoteTransaction(Request request, Connection.Callback callback) {
        /*
        ImmutablePair<Boolean, TransactionRequest> booleanTransactionRequestPair =
                driver.decodeTransactionRequest(request);
        if (booleanTransactionRequestPair.getLeft()) {

            TransactionRequest transactionRequest = booleanTransactionRequestPair.getValue();
            // TODO: check request

            // fail or return
        }
        */

        request.setResourceInfo(resourceInfo);
        chooseConnection().asyncSend(request, callback);
    }

    public Response onRemoteTransaction(Request request) {
        CompletableFuture<Response> completableFuture = new CompletableFuture<>();

        onRemoteTransaction(
                request,
                new Connection.Callback() {
                    @Override
                    public void onResponse(Response response) {
                        completableFuture.complete(response);
                    }
                });

        try {
            return completableFuture.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            Response response = new Response();
            response.setErrorCode(StubQueryStatus.TIMEOUT);
            response.setErrorMessage("onRemoteTransaction completableFuture exception: " + e);
            logger.error("onRemoteTransaction timeout, resource: " + getResourceInfo());
            return response;
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
