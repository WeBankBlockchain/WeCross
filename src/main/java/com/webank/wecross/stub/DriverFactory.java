package com.webank.wecross.stub;

public interface DriverFactory {
	/**
	 * create a driver with connection
	 * @param connection
	 * @return
	 */
	public Driver newDriver(Connection connection);
}
