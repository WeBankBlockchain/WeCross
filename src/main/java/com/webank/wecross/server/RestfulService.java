package com.webank.wecross.server;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RestfulService {
	@RequestMapping("/call")
	public void call(String uri, String[] args) {
		
	}
	
	@RequestMapping("/sendTransaction")
	public void sendTransaction(String uri, String[] args) {
		
	}
}
