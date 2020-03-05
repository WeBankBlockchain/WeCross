package com.webank.wecross.driver;

public interface ConnectionFactory {
	/**
	 * create a connection
	 * @return Connection
	 */
	public Connection newConnection();
}
