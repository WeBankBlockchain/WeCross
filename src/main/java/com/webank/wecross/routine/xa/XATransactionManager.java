package com.webank.wecross.routine.xa;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
				path.setNetwork(p.getNetwork());
				path.setChain(p.getChain());
				return path;
			}).distinct().collect(Collectors.toSet());
			
			for(Path path: chains) {
				Zone zone = zoneManager.getZone(path.getNetwork());
				Chain chain = zone.getChain(path.getChain());
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
