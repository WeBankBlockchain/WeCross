package com.webank.wecross.resource;

import com.webank.wecross.peer.Peer;
import com.webank.wecross.restserver.request.TransactionRequest;
import com.webank.wecross.restserver.response.TransactionResponse;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Request;
import com.webank.wecross.stub.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Resource {
    private String type;
    private Driver driver;
    private Map<Peer, Connection> connections = new HashMap<Peer, Connection>();
    private Random random = new Random();
    int distance = 0;

    public void addConnection(Peer peer, Connection connection) {
        connections.put(peer, connection);
    }

    public void removeConnection(Peer peer) {
        connections.remove(peer);
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

    public TransactionResponse call(TransactionRequest request) {
        return driver.call(request, chooseConnection());
    }

    public TransactionResponse sendTransaction(TransactionRequest request) {
        return driver.sendTransaction(request, chooseConnection());
    }

    public Response onRemoteTransaction(Request request) {
        TransactionRequest transactionRequest = driver.decodeTransactionRequest(request.getData());

        // TODO: check request
        transactionRequest.getArgs();

        return chooseConnection().send(request);
    }

    public void registerEventHandler(EventCallback callback) {}

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
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
}
