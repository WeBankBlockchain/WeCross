package com.webank.wecross.stub;

import java.util.List;

public interface Connection {
	/**
	 * send request to blockchain
	 * @param request
	 * @return
	 */
	Response send(Request request);
	
	/**
	 * get resources name
	 */
	List<String> getResources();
}
