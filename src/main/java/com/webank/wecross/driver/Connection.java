package com.webank.wecross.driver;

public interface Connection {
	/**
	 * send request to blockchain
	 * @param request
	 * @return
	 */
	Response send(Request request);
}
