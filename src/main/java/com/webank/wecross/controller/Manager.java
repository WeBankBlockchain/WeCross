package com.webank.wecross.controller;

import java.util.List;
import com.webank.wecross.bcp.Resource;

import com.webank.wecross.bcp.Stub;
import com.webank.wecross.bcp.URI;

public class Manager {
	private List<StubConfig> stubs;
	
	public Stub getStub(URI uri) {
		for(StubConfig stubConfig: stubs) {
			stubConfig.getPattern();
			
			if(true) {
				return stubConfig.getStub();
			}
		}
		
		return null;
	}
	
	public Resource getResource(URI uri) {
		Stub stub = getStub(uri);
		return stub.getResource(uri);
	}
}