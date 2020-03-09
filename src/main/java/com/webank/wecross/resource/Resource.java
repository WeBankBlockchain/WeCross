package com.webank.wecross.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.webank.wecross.peer.Peer;
import com.webank.wecross.restserver.request.TransactionRequest;
import com.webank.wecross.restserver.response.TransactionResponse;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;

public class Resource {
	private Driver driver;
	private Map<Peer, Connection> connections = new HashMap<Peer, Connection>();
	private Random random = new Random();
	
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
		if(connections.size() == 1) {
			return (Connection) connections.values().toArray()[0];
		}
		else {
			int index = random.nextInt(connections.size());
			return (Connection) connections.values().toArray()[index];
		}
	}
	
    public String getType() {
    	return "Resource";
    }

    public TransactionResponse call(TransactionRequest request) {
    	return driver.call(request, chooseConnection());
    }

    public TransactionResponse sendTransaction(TransactionRequest request) {
    	return driver.sendTransaction(request, chooseConnection());
    }

    public void registerEventHandler(EventCallback callback) {
    	
    }

    // TransactionRequest createRequest();

    public int getDistance() {
    	return 0;
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

	public Random getRandom() {
		return random;
	}

	public void setRandom(Random random) {
		this.random = random;
	}
}
