package com.webank.wecross.routine.xa;

import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stub.TransactionContext;
import com.webank.wecross.stub.TransactionException;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.TransactionResponse;
import com.webank.wecross.zone.Chain;
import com.webank.wecross.zone.Zone;
import com.webank.wecross.zone.ZoneManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XATransactionManager {
	private Logger logger = LoggerFactory.getLogger(XATransactionManager.class);
    private ZoneManager zoneManager;

    interface Callback {
        public void onResponse(Exception e, int result);
    }
    
    public Map<String, List<Path>> getChainPaths(List<Path> resources) {
    	Map<String, List<Path>> zone2Path = new HashMap<String, List<Path>>();
        for (Path path : resources) {
            String key = path.getZone() + "." + path.getChain();
            if (zone2Path.get(key) == null) {
                zone2Path.put(key, new ArrayList<Path>());
            }

            zone2Path.get(key).add(path);
        }
        
        return zone2Path;
    }
    
    public void asyncPrepare(String transactionID, Account account, List<Path> resources, Callback callback) {
        try {
        	Map<String, List<Path>> zone2Path = getChainPaths(resources);

            for (Map.Entry<String, List<Path>> entry : zone2Path.entrySet()) {
                Path chainPath = entry.getValue().get(0);

                Chain chain = zoneManager.getZone(chainPath).getChain(chainPath);

                // send prepare transaction
                TransactionRequest transactionRequest = new TransactionRequest();
                transactionRequest.setMethod("startTransaction");
                String[] args = new String[entry.getValue().size() + 1];
                args[0] = transactionID;
                for(int i=0; i<entry.getValue().size(); ++i) {
                	args[i + 1] = entry.getValue().get(i).toURI();
                }
                transactionRequest.setArgs(args);
                Resource resource = zoneManager.getResource(Path.decode("WeCrossProxy"));
                
                TransactionContext<TransactionRequest> transactionContext = new TransactionContext<TransactionRequest>(transactionRequest, account, resource.getResourceInfo(), chain.getBlockHeaderManager());

                resource.asyncSendTransaction(transactionContext, (error, response) -> {
                	if(error != null) {
                		logger.error("Send prepare transaction error", error);
                		
                		callback.onResponse(error, -1);
                		return;
                	}
                	
                	callback.onResponse(null, 0);
                });
            }
        } catch (Exception e) {
        	logger.error("Prepare error", e);
        }
    };

    public void asyncCommit(String transactionID, Account account, Callback callback) {
    	try {
        	// Map<String, List<Path>> zone2Path = getChainPaths(resources);
    		Map<String, List<Path>> zone2Path = new HashMap<>();

            for (Map.Entry<String, List<Path>> entry : zone2Path.entrySet()) {
                Path chainPath = entry.getValue().get(0);

                Chain chain = zoneManager.getZone(chainPath).getChain(chainPath);

                // send prepare transaction
                TransactionRequest transactionRequest = new TransactionRequest();
                transactionRequest.setMethod("commitTransaction");
                String[] args = new String[entry.getValue().size() + 1];
                args[0] = transactionID;
                for(int i=0; i<entry.getValue().size(); ++i) {
                	args[i + 1] = entry.getValue().get(i).toURI();
                }
                transactionRequest.setArgs(args);
                Resource resource = zoneManager.getResource(Path.decode("WeCrossProxy"));
                
                TransactionContext<TransactionRequest> transactionContext = new TransactionContext<TransactionRequest>(transactionRequest, account, resource.getResourceInfo(), chain.getBlockHeaderManager());

                resource.asyncSendTransaction(transactionContext, (error, response) -> {
                	if(error != null) {
                		logger.error("Send prepare transaction error", error);
                		
                		callback.onResponse(error, -1);
                		return;
                	}
                	
                	callback.onResponse(null, 0);
                });
            }
        } catch (Exception e) {
        	logger.error("Prepare error", e);
        }
    };

    public void asyncRollback(String transactionID, Account account, Callback callback) {}

    interface TransactionHistoryCallback {
        public void onResponse(List<XATransactionStep> transactionSteps);
    }

    public void asyncGetTransactionHistory(
            String transactionID, TransactionHistoryCallback callback) {}
}
