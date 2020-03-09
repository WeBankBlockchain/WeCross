package com.webank.wecross.stub;

import java.util.Map;

public class StubManager {
	private Map<String, Driver> drivers;
	
	public void addDriver(String driverType, Driver driver) {
		drivers.put(driverType, driver);
	}
	
	public Driver getDriver(String driverType) {
		return drivers.get(driverType);
	}
}
