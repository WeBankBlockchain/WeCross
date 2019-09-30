package com.webank.wecross.server;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecross.bcp.Resource;
import com.webank.wecross.bcp.URI;
import com.webank.wecross.core.StubManager;

@RestController
public class RestfulService {
	private StubManager stubManager;
	
	@RequestMapping("/call")
	public void call(String uri, String[] args) {
		try {
			Resource resource = stubManager.getResource(URI.decode(uri));
		} catch (Exception e) {
		}
	}
	
	@RequestMapping("/sendTransaction")
	public void sendTransaction(String uri, String[] args) {
		
	}
	
	public StubManager getStubManager() {
		return stubManager;
	}

	public void setStubManager(StubManager stubManager) {
		this.stubManager = stubManager;
	}
}
