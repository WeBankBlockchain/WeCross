package com.webank.wecross.stub;

public interface Connection {
	/**
	 * send request to blockchain
	 * @param request
	 * @return
	 */
	Response send(Request request);
}
