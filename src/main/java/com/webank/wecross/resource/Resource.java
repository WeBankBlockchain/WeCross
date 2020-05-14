package com.webank.wecross.resource;

import com.webank.wecross.peer.Peer;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Request;
import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stub.Response;
import com.webank.wecross.stub.TransactionContext;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.TransactionResponse;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Resource {
    private String type;
    private Driver driver;
    private Map<Peer, Connection> connections = new HashMap<Peer, Connection>();
    private ResourceInfo resourceInfo;
    private ResourceBlockHeaderManager resourceBlockHeaderManager;
    boolean hasLocalConnection = false;
    private Random random = new SecureRandom();

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
        if (connections.size() == 1) {
            return (Connection) connections.values().toArray()[0];
        } else {
            int index = random.nextInt(connections.size());
            return (Connection) connections.values().toArray()[index];
        }
    }

    public abstract static class Callback {
        public abstract void onTransactionResponse(TransactionResponse transactionResponse);
    }

    public TransactionResponse call(TransactionContext<TransactionRequest> request) {
        return driver.call(request, chooseConnection());
    }

    public void asyncCall(
            TransactionContext<TransactionRequest> request, Resource.Callback callback) {
        driver.asyncCall(
                request,
                chooseConnection(),
                new Driver.Callback() {
                    @Override
                    public void onTransactionResponse(TransactionResponse transactionResponse) {
                        callback.onTransactionResponse(transactionResponse);
                    }
                });
    }

    public TransactionResponse sendTransaction(TransactionContext<TransactionRequest> request) {
        return driver.sendTransaction(request, chooseConnection());
    }

    public void asyncSendTransaction(
            TransactionContext<TransactionRequest> request, Resource.Callback callback) {
        driver.asyncSendTransaction(
                request,
                chooseConnection(),
                new Driver.Callback() {
                    @Override
                    public void onTransactionResponse(TransactionResponse transactionResponse) {
                        callback.onTransactionResponse(transactionResponse);
                    }
                });
    }

    public Response onRemoteTransaction(Request request) {
        if (driver.isTransaction(request)) {
            TransactionContext<TransactionRequest> transactionRequest =
                    driver.decodeTransactionRequest(request.getData());

            // TODO: check request

            // fail or return
        }

        request.setResourceInfo(resourceInfo);
        return chooseConnection().send(request);
    }

    public void registerEventHandler(EventCallback callback) {}

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public ResourceBlockHeaderManager getResourceBlockHeaderManager() {
        return resourceBlockHeaderManager;
    }

    public void setResourceBlockHeaderManager(
            ResourceBlockHeaderManager resourceBlockHeaderManager) {
        this.resourceBlockHeaderManager = resourceBlockHeaderManager;
    }

    public boolean isHasLocalConnection() {
        return hasLocalConnection;
    }
}
