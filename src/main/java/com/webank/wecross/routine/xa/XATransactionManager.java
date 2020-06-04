package com.webank.wecross.routine.xa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.zone.Chain;
import com.webank.wecross.zone.Zone;
import com.webank.wecross.zone.ZoneManager;

public class XATransactionManager {
	private ZoneManager zoneManager;
	private Account account;
	
	interface Callback {
		public void onResponse(Exception e, int result);
	}
	
	public void asyncPrepare(String transactionID, List<Path> resources, Callback callback) {
		try {
			Set<Path> chains = resources.parallelStream().map((p) -> {
				Path path = new Path();
				path.setZone(p.getZone());
				path.setChain(p.getChain());
				return path;
			}).distinct().collect(Collectors.toSet());
			
			Map<String, List<Path> > zone2path = new HashMap<String, List<Path>>();
			for(Path path: chains) {
				String key = path.getZone() + "." + path.getChain();
				if(zone2path.get(key) == null) {
					zone2path.put(key, new ArrayList<Path>());
				}
				
				zone2path.get(key).add(path);
			}
			
			for(Map.Entry<String, List<Path> > entry: zone2path.entrySet()) {
				Path chainPath = entry.getValue().get(0);
				
				Zone zone = zoneManager.getZone(chainPath);
				Chain chain = zone.getChain(chainPath);
				Connection connection = chain.chooseConnection();
				
				// send prepare transaction
				TransactionRequest transactionRequest = new TransactionRequest();
				transactionRequest.setMethod("prepare");
				transactionRequest.setArgs(new String[] {""});
				Resource resource = zoneManager.getResource(Path.decode("SystemProxy"));
				
				ResourceInfo resourceInfo = new ResourceInfo();
				// resourceInfo.setStubType(connection.);
				
				// resource.asyncSendTransaction(request, callback);
			}
		}
		catch(Exception e) {
			
		}
	};
	
	public void asyncCommit(String trnasactionID, Callback callback) {
		
	};
	
	public void asyncRollback(String transactionID, Callback callback) {
		
	}
	
	interface TransactionHistoryCallback {
		public void onResponse(List<XATransactionStep> transactionSteps);
	}
	
	public void asyncGetTransactionHistory(String transactionID, TransactionHistoryCallback callback) {
		
	}
}
