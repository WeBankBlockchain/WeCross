package com.webank.wecross.resource;

import com.webank.wecross.account.Account;
import com.webank.wecross.peer.Peer;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Request;
import com.webank.wecross.stub.Response;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.TransactionResponse;
import com.webank.wecross.stub.WithAccount;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Resource {
    private String type;
    private Driver driver;
    private Map<Peer, Connection> connections = new HashMap<Peer, Connection>();
    boolean hasLocalConnection = false;
	private Random random = new SecureRandom();

    public void addConnection(Peer peer, Connection connection) {
    	if(!hasLocalConnection) {
    		if(peer == null) {
    			connections.clear();
    			hasLocalConnection = true;
    		}
    		
    		connections.put(peer, connection);    		
    	}
    }

    public void removeConnection(Peer peer) {
    	if(!hasLocalConnection) {
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

    public TransactionResponse call(TransactionRequest request, Account account) {
        return driver.call(
                new WithAccount<TransactionRequest>(request, account), chooseConnection());
    }

    public TransactionResponse sendTransaction(TransactionRequest request, Account account) {
        return driver.sendTransaction(
                new WithAccount<TransactionRequest>(request, account), chooseConnection());
    }

    public Response onRemoteTransaction(Request request) {
        WithAccount<TransactionRequest> transactionRequest =
                driver.decodeTransactionRequest(request.getData());

        // TODO: check request
        transactionRequest.getData().getArgs();

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
    
    public boolean isHasLocalConnection() {
		return hasLocalConnection;
	}
}
