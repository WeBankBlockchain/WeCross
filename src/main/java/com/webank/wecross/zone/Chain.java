package com.webank.wecross.zone;

import com.webank.wecross.peer.Peer;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.BlockHeader;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Chain {
    private Logger logger = LoggerFactory.getLogger(Chain.class);
    private Map<Peer, Connection> connections = new HashMap<Peer, Connection>();
    private Map<String, Resource> resources = new HashMap<String, Resource>();
    private Driver driver;
    private String path;

    public int getBlockNumber() {
        return 0;
    }

    public BlockHeader getBlockHeader(int blockNumber) {
        return null;
    }
    
    public void addConnection(Peer peer, Connection connection) {
    	connections.put(peer, connection);
    }
    
    public Connection getConnection(Peer peer) {
    	return connections.get(peer);
    }
    
    public void removeConnection(Peer peer) {
    	connections.remove(peer);
    }

    public Map<String, Resource> getResources() {
        return resources;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setResources(Map<String, Resource> resources) {
        this.resources = resources;
    }
    
    public Driver getDriver() {
		return driver;
	}

	public void setDriver(Driver driver) {
		this.driver = driver;
	}

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
