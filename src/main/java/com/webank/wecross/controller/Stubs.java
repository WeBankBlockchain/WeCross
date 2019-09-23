package com.webank.wecross.controller;

import java.util.List;

import com.webank.wecross.bcp.Stub;
import com.webank.wecross.bcp.URI;

public class Stubs {
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
}
