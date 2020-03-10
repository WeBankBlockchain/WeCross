package com.webank.wecross.stub;

public interface StubFactory {
	/**
	 * create a driver with connection
	 * @param connection
	 * @return
	 */
	public Driver newDriver();
	
	/**
	 * create a connection
	 * @return Connection
	 */
	public Connection newConnection();
}
