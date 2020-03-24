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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Resource {
    private Logger logger = LoggerFactory.getLogger(Resource.class);

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

    private Connection chooseConnection() {
        if (connections.size() == 1) {
            return (Connection) connections.values().toArray()[0];
        } else {
            int index = random.nextInt(connections.size());
            return (Connection) connections.values().toArray()[index];
        }
    }

    public TransactionResponse call(TransactionContext<TransactionRequest> request) {
        logger.info(request.toString());
        return driver.call(request, chooseConnection());
    }

    public TransactionResponse sendTransaction(TransactionContext<TransactionRequest> request) {
        logger.info(request.toString());
        return driver.sendTransaction(request, chooseConnection());
    }

    public Response onRemoteTransaction(Request request) {
        logger.info(request.toString());
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
